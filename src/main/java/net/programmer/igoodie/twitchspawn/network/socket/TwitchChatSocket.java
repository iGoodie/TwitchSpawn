package net.programmer.igoodie.twitchspawn.network.socket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.programmer.igoodie.twitchspawn.TwitchSpawnClient;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.easteregg.Developers;
import net.programmer.igoodie.twitchspawn.network.NetworkManager;
import net.programmer.igoodie.twitchspawn.network.Platform;
import net.programmer.igoodie.twitchspawn.network.SocketManager;
import net.programmer.igoodie.twitchspawn.network.packet.EventPacket;
import net.programmer.igoodie.twitchspawn.network.socket.base.WebSocketBase;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.util.CooldownBucket;
import okhttp3.Response;
import okhttp3.WebSocket;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class TwitchChatSocket extends WebSocketBase {

    private final CooldownBucket cooldownBucket = new CooldownBucket(0, 30 * 1000);
    private boolean startedWithProperToken;

    public TwitchChatSocket() {
        super(Platform.TWITCH_CHAT_IRC);
    }

    /* ----------------------------- */

    public static class MessageModel {
        public static final Pattern TWITCH_CHAT_PATTERN = Pattern.compile("^@(?<tags>.*?) (:(?<user>.*?)!.*?\\.tmi\\.twitch\\.tv) PRIVMSG #(?<channel>.*?) :(?<msg>.*)$");

        protected String raw;

        public String username;
        public String message;
        public Set<String> badges; // admin, bits, broadcaster, global_mod, moderator, subscriber, staff, turbo, vip, glhf-pledge
        public int subscriptionMonths;
        public boolean isDeveloper;

        public MessageModel(String raw) {
            this.raw = raw;
            this.badges = new HashSet<>();

            Matcher matcher = TWITCH_CHAT_PATTERN.matcher(raw);

            if (matcher.matches()) {
                Map<String, String> tags = parseTags(matcher.group("tags"));

                String displayName = tags.getOrDefault("display-name", "");
                this.username = displayName.isEmpty() ? matcher.group("user") : displayName;

                Stream.of(tags.getOrDefault("badges", "").split(",")).forEach(badgeRaw -> {
                    if (badgeRaw.isEmpty()) return;
                    String[] parts = badgeRaw.split("/", 2);
                    String badgeName = parts[0];
                    String badgeVersion = parts[1];
                    badges.add(badgeName);
                });

                Stream.of(tags.getOrDefault("badge-info", "").split(",")).forEach(infoRaw -> {
                    if (infoRaw.isEmpty()) return;
                    String[] parts = infoRaw.split("/", 2);
                    String infoName = parts[0];
                    String infoValue = parts[1];
                    if (infoName.equals("subscriber"))
                        subscriptionMonths = Integer.parseInt(infoValue);
                });

                this.isDeveloper = Developers.TWITCH_NICKS.contains(this.username);

                this.message = matcher.group("msg");
            }
        }

        public static Map<String, String> parseTags(String tagsRaw) {
            Map<String, String> tags = new HashMap<>();

            for (String tagPairRaw : tagsRaw.split(";")) {
                String[] tagPair = tagPairRaw.split("=", 2);
                tags.put(tagPair[0], tagPair[1]);
            }

            return tags;
        }

        public static boolean matches(String raw) {
            return TWITCH_CHAT_PATTERN.matcher(raw).matches();
        }
    }

    /* ----------------------------- */

    @Override
    public void onOpen(@Nonnull WebSocket socket, @Nonnull Response response) {
        super.onOpen(socket, response);

        TwitchSpawnClient.LOGGER.info("Attempting to connect Twitch Chat of {}", ConfigManager.CLIENT_CREDS.twitchNickname);

        if (!ConfigManager.CLIENT_CREDS.twitchChatToken.startsWith("oauth:")) {
            this.stop();
            TwitchSpawnClient.LOGGER.info("Failed to connect Twitch Chat of {}", ConfigManager.CLIENT_CREDS.twitchNickname);
            return;
        }

        socket.send("PASS " + ConfigManager.CLIENT_CREDS.twitchChatToken);
        socket.send("NICK " + ConfigManager.CLIENT_CREDS.twitchNickname.toLowerCase());
        socket.send("JOIN #" + ConfigManager.CLIENT_CREDS.twitchNickname.toLowerCase());
        socket.send("CAP REQ :twitch.tv/tags");
//        socket.send("PRIVMSG #" + ConfigManager.CLIENT_CREDS.twitchNickname.toLowerCase()
//                + " :TwitchSpawn now connected to the chat! Hey folks!");

        startedWithProperToken = true;

        // https://twitchapps.com/tmi/
    }

    @Override
    public void onClosing(@Nonnull WebSocket webSocket, int code, @Nonnull String reason) {
        TwitchSpawnClient.LOGGER.info("Disconnected from {}'s Twitch Chat connection. (intentional)", ConfigManager.CLIENT_CREDS.twitchNickname);
        if (startedWithProperToken) {
            SocketManager.stop();
            startedWithProperToken = false;
        }
    }

    @Override
    public void onMessage(@Nonnull WebSocket socket, @Nonnull String text) {
        Stream.of(text.split("\r?\n")).map(String::trim).forEach(message -> {
            if (message.equals("PING :tmi.twitch.tv")) {
                socket.send("PONG :tmi.twitch.tv");

            } else if (MessageModel.matches(message)) {
                MessageModel twitchChatMessage = new MessageModel(message);
                if (twitchChatMessage.message.startsWith("!"))
                    onChatMessage(socket, twitchChatMessage);

            } else if (message.contains(":tmi.twitch.tv NOTICE")) {
                if (message.contains("Improperly formatted auth")) {
                    // Intentionally left empty/malformed.
                    TwitchSpawnClient.LOGGER.info("Disconnected from {}'s Twitch Chat connection. (no token)", ConfigManager.CLIENT_CREDS.twitchNickname);
                    SocketManager.stop();

                } else if (message.contains("Login authentication failed")) {
                    // Uh oh invalid token?
                    TwitchSpawnClient.LOGGER.warn("Disconnected from {}'s Twitch Chat connection. (unauthorized)", ConfigManager.CLIENT_CREDS.twitchNickname);
                    SocketManager.stop();
                }
            }
        });
    }

    protected void onChatMessage(WebSocket socket, MessageModel twitchChatMessage) {
//        CooldownBucket cooldownBucket = cooldownBuckets.get(streamer.twitchNick);

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        EventArguments eventArguments = new EventArguments("chat", "twitch");
        eventArguments.streamerNickname = player == null ? "Dev" : player.getDisplayName().getString();
        eventArguments.actorNickname = twitchChatMessage.username;
        eventArguments.message = twitchChatMessage.message;
        eventArguments.subscriptionMonths = twitchChatMessage.subscriptionMonths;
        eventArguments.chatBadges = twitchChatMessage.badges;

        // Pass the model to the handler
        if (cooldownBucket.canConsume(twitchChatMessage.username)) {
            cooldownBucket.consume(twitchChatMessage.username);
            NetworkManager.CHANNEL.sendToServer(
                    new EventPacket(eventArguments)
            );
        }

//        if (cooldownBucket.hasGlobalCooldown()) {
//            TwitchSpawnClient.LOGGER.info("Still has {} seconds global cooldown.", cooldownBucket.getGlobalCooldown());
//
//        } else if (cooldownBucket.canConsume(twitchChatMessage.username)) {
//            ConfigManager.RULESET_COLLECTION.handleEvent(eventArguments, cooldownBucket);
//
//        } else if (ConfigManager.RULESET_COLLECTION.getRuleset(streamer.minecraftNick).willPerform(eventArguments)) {
//            if (ConfigManager.PREFERENCES.chatWarnings) {
//                socket.send("PRIVMSG #" + streamer.twitchNick.toLowerCase()
//                        + String.format(" :@%s, you still have %s second(s), before you can trigger another action",
//                        twitchChatMessage.username, cooldownBucket.getCooldown(twitchChatMessage.username) / 1000));
//            }
//        }

    }

}
