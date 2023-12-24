package net.programmer.igoodie.twitchspawn.tracer.chat;

import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.configuration.CredentialsConfig;
import net.programmer.igoodie.twitchspawn.tracer.Platform;
import net.programmer.igoodie.twitchspawn.tracer.TraceManager;
import net.programmer.igoodie.twitchspawn.tracer.WebSocketTracer;
import net.programmer.igoodie.twitchspawn.tracer.model.TwitchChatMessage;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.util.CooldownBucket;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class TwitchChatTracer extends WebSocketTracer {

    // Streamer Nickname -> CooldownBucket
    public Map<String, CooldownBucket> cooldownBuckets;

    public TwitchChatTracer(TraceManager manager) {
        super(Platform.TWITCH_CHAT_IRC, manager);
        this.cooldownBuckets = new HashMap<>();
    }

    @Override
    public void start() {
        for (CredentialsConfig.Streamer streamer : ConfigManager.CREDENTIALS.streamers) {
            WebSocketListener socket = createSocket(streamer);
            this.sockets.add(startClient(socket));
            this.cooldownBuckets.put(streamer.twitchNick,
                    new CooldownBucket(ConfigManager.PREFERENCES.chatGlobalCooldown,
                            ConfigManager.PREFERENCES.chatIndividualCooldown));
        }
    }

    @Override
    public void stop() {
        for (WebSocket socket : this.sockets) {
            if (!socket.close(1000, null)) {
                socket.cancel();
            }
        }
        this.cooldownBuckets.clear();
    }

    /* --------------------------------------------------- */

    @Override
    protected void onOpen(CredentialsConfig.Streamer streamer, WebSocket socket, Response response) {
        TwitchSpawn.LOGGER.info("Attempting to connect Twitch Chat of {}", streamer.twitchNick);

        socket.send("PASS " + streamer.tokenChat);
        socket.send("NICK " + streamer.twitchNick.toLowerCase());
        socket.send("JOIN #" + streamer.twitchNick.toLowerCase());
        socket.send("CAP REQ :twitch.tv/tags");
        socket.send("PRIVMSG #" + streamer.twitchNick.toLowerCase()
                + " :TwitchSpawn now connected to the chat! Hey folks!");

        // https://twitchapps.com/tmi/
    }

    @Override
    protected void onClosing(CredentialsConfig.Streamer streamer, WebSocket socket, int code, String reason) {
        TwitchSpawn.LOGGER.info("Disconnected from {}'s Twitch Chat connection. (intentional)", streamer.minecraftNick);
    }

    @Override
    protected void onMessage(CredentialsConfig.Streamer streamer, WebSocket socket, String text) {
        Stream.of(text.split("\r?\n")).map(String::trim).forEach(message -> {
            if (message.equals("PING :tmi.twitch.tv")) {
                socket.send("PONG :tmi.twitch.tv");

            } else if (TwitchChatMessage.matches(message)) {
                TwitchChatMessage twitchChatMessage = new TwitchChatMessage(message);
                onChatMessage(streamer, twitchChatMessage, socket);

            } else if (message.contains(":tmi.twitch.tv NOTICE")) {
                if (message.contains("Improperly formatted auth")) {
                    // Intentionally left empty/malformed.
                    TwitchSpawn.LOGGER.info("Disconnected from {}'s Twitch Chat connection. (no token)", streamer.minecraftNick);
                    socket.cancel();

                } else if (message.contains("Login authentication failed")) {
                    // Uh oh invalid token?
                    TwitchSpawn.LOGGER.warn("Disconnected from {}'s Twitch Chat connection. (unauthorized)", streamer.minecraftNick);
                    manager.stop(null, streamer.twitchNick + " unauthorized by the Twitch Chat server.");
                }
            }
        });
    }

    protected void onChatMessage(CredentialsConfig.Streamer streamer, TwitchChatMessage twitchChatMessage, WebSocket socket) {
        CooldownBucket cooldownBucket = cooldownBuckets.get(streamer.twitchNick);

        EventArguments eventArguments = new EventArguments("chat", "twitch");
        eventArguments.streamerNickname = streamer.minecraftNick;
        eventArguments.actorNickname = twitchChatMessage.username;
        eventArguments.message = twitchChatMessage.message;
        eventArguments.subscriptionMonths = twitchChatMessage.subscriptionMonths;
        eventArguments.chatBadges = twitchChatMessage.badges;

        if (cooldownBucket.hasGlobalCooldown()) {
            TwitchSpawn.LOGGER.info("Still has {} seconds global cooldown.", cooldownBucket.getGlobalCooldown());

        } else if (cooldownBucket.canConsume(twitchChatMessage.username)) {
            ConfigManager.RULESET_COLLECTION.handleEvent(eventArguments, cooldownBucket);

        } else if (ConfigManager.RULESET_COLLECTION.getRuleset(streamer.minecraftNick).willPerform(eventArguments)) {
            if (ConfigManager.PREFERENCES.chatWarnings) {
                socket.send("PRIVMSG #" + streamer.twitchNick.toLowerCase()
                        + String.format(" :@%s, you still have %s second(s), before you can trigger another action",
                        twitchChatMessage.username, cooldownBucket.getCooldown(twitchChatMessage.username) / 1000));
            }
        }

    }

    public static void main(String[] args) {
        new TwitchChatTracer(new TraceManager()).start();
    }

}
