package net.programmer.igoodie.twitchspawn.network.socket;

import io.socket.client.IO;
import io.socket.client.Socket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ClientCredentialsConfig;
import net.programmer.igoodie.twitchspawn.network.NetworkManager;
import net.programmer.igoodie.twitchspawn.network.Platform;
import net.programmer.igoodie.twitchspawn.network.SocketManager;
import net.programmer.igoodie.twitchspawn.network.packet.EventPacket;
import net.programmer.igoodie.twitchspawn.network.socket.base.SocketIOBase;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEventPair;
import net.programmer.igoodie.twitchspawn.tslanguage.event.builder.EventBuilder;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLEventKeyword;
import net.programmer.igoodie.twitchspawn.util.JSONUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StreamlabsSocket extends SocketIOBase {

    protected boolean authorized;
    protected Map<String, Long> subGiftHandleTimestamps;

    public StreamlabsSocket() {
        super(Platform.STREAMLABS);
        this.subGiftHandleTimestamps = new HashMap<>();
    }

    @Override
    protected IO.Options generateOptions(ClientCredentialsConfig config) {
        IO.Options options = super.generateOptions(config);
        options.query = "token=" + config.platformToken;
        return options;
    }

    @Override
    public boolean validateCredentials(ClientCredentialsConfig config) {
        return config.platform != null
                && !config.platformToken.isEmpty();
    }

    @Override
    protected void onConnect(Socket socket, Object... args) {
        TwitchSpawn.LOGGER.info("Connected to Streamlabs Socket API with the token successfully!");
        authorized = true;
    }

    @Override
    protected void onDisconnect(Socket socket, Object... args) {
        TwitchSpawn.LOGGER.info("Disconnected from Streamlabs Socket connection. ({})",
                authorized ? "intentional" : "unauthorized");
        SocketManager.stop();
        this.subGiftHandleTimestamps.clear();
    }

    /* -------------------- */

    @Override
    protected void bindEvents() {
        super.bindEvents();
        this.socket.on("event", args -> onEvent(socket, args));
    }

    protected void onEvent(Socket socket, Object... args) {
        JSONObject event = (JSONObject) args[0];
        JSONArray messages = extractMessages(event);

        if (messages == null) {
            TwitchSpawn.LOGGER.info("Received unexpected Streamlabs packet -> {}", event);
            return; // Contains no message (in expected format), stop here
        }

        String eventType = JSONUtils.extractFrom(event, "type", String.class, null);
        String eventFor = JSONUtils.extractFrom(event, "for", String.class, "streamlabs");
        TSLEventPair eventPair = new TSLEventPair(eventType, eventFor.replace("_account", ""));

        JSONUtils.forEach(messages, message -> {
            TwitchSpawn.LOGGER.info("Received Streamlabs packet {} -> {}",
                    new TSLEventPair(eventType, eventFor), message);

            // If it's a sub gift -- XXX: Streamlabs notify Sub Gifts twice for some reason :thinking:
            if (Objects.equals(TSLEventKeyword.ofPair(eventPair), TSLEventKeyword.TWITCH_SUBSCRIPTION_GIFT.eventName)) {
                String subGiftId = JSONUtils.extractFrom(message, "_id", String.class, null);
                if (subGiftId != null) {
                    Long prevTimestamp = this.subGiftHandleTimestamps.get(subGiftId);
                    long now = System.currentTimeMillis();
                    if (prevTimestamp != null && now - prevTimestamp <= 5000L) {
                        TwitchSpawn.LOGGER.warn("Sub gift was already handled less than 5 seconds ago. Skipping -> {}", message);
                        return;
                    } else {
                        this.subGiftHandleTimestamps.put(subGiftId, now);
                    }
                }
            }

            // Fetch the appropriate builder
            EventBuilder eventBuilder = TSLEventKeyword.getBuilder(eventPair);

            // Unregistered event alias
            if (eventBuilder == null)
                return; // Stop here, do not handle

            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;

            // Build arguments
            EventArguments eventArguments = eventBuilder.build(
                    player == null ? "Dev" : player.getDisplayName().getString(),
                    eventPair,
                    message,
                    Platform.STREAMLABS);

            // Build failed for an unknown reason
            if (eventArguments == null) {
                TwitchSpawn.LOGGER.warn("{} was not able to build arguments from incoming data -> {}",
                        eventBuilder.getClass().getSimpleName(), message.toString());
                return;
            }

            // Pass the model to the handler
            NetworkManager.CHANNEL.sendToServer(
                    new EventPacket(eventArguments)
            );
        });
    }

    private JSONArray extractMessages(JSONObject event) {
        try {
            Object messageField = event.get("message");

            if (messageField instanceof JSONArray)
                return JSONUtils.extractFrom(event, "message", JSONArray.class, new JSONArray());

            else if (messageField instanceof JSONObject)
                return new JSONArray().put(messageField);

            return null;

        } catch (JSONException e) {
            return null;
        }
    }

}
