package net.programmer.igoodie.twitchspawn.network;

import io.socket.client.IO;
import io.socket.client.Socket;
import net.minecraft.client.Minecraft;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.client.gui.StatusIndicatorOverlay;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.network.packet.EventPacket;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEventPair;
import net.programmer.igoodie.twitchspawn.tslanguage.event.builder.EventBuilder;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLEventKeyword;
import net.programmer.igoodie.twitchspawn.util.JSONUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StreamlabsSocket {

    public static final StreamlabsSocket INSTANCE = new StreamlabsSocket();

    public Socket socket;
    public boolean running;
    public boolean authorized;
    public Map<String, Long> subGiftHandleTimestamps;

    private StreamlabsSocket() {}

    protected IO.Options generateOptions() {
        IO.Options options = new IO.Options();
        options.forceNew = true;
        options.reconnection = true;
        options.transports = new String[]{"websocket"};
        options.query = "token=" + ConfigManager.CLIENT_CREDS.streamlabsToken;
        return options;
    }

    protected Socket createSocket() {
        try {
            IO.Options options = generateOptions();
            Socket socket = IO.socket(Platform.STREAMLABS.url, options);

            socket.on(Socket.EVENT_CONNECT, args -> onConnect(socket, args));
            socket.on(Socket.EVENT_DISCONNECT, args -> onDisconnect(socket, args));
            socket.on("event", args -> onLiveEvent(socket, args));

            return socket;

        } catch (URISyntaxException e) {
            throw new InternalError("Invalid URI for " + Platform.STREAMLABS.name + " = " + Platform.STREAMLABS.url);
        }
    }

    public void start() {
        this.socket = createSocket();
        this.socket.connect();
        running = true;
        this.subGiftHandleTimestamps = new HashMap<>();
        StatusIndicatorOverlay.setRunning(true);
    }

    public void stop() {
        if (socket != null && socket.connected()) this.socket.disconnect();
        this.socket = null;
        running = false;
        StatusIndicatorOverlay.setRunning(false);
        TwitchPubSubSocket.INSTANCE.stop();
    }

    protected void onConnect(Socket socket, Object... args) {
        TwitchSpawn.LOGGER.info("Connected to Streamlabs Socket API with the token successfully!");
        authorized = true;
    }

    protected void onDisconnect(Socket socket, Object... args) {
        TwitchSpawn.LOGGER.info("Disconnected from Streamlabs Socket connection. ({})",
                authorized ? "intentional" : "unauthorized");
        socket.close();
//        socket.disconnect();
        stop();
    }

    protected void onLiveEvent(Socket socket, Object... args) {
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
                    if (prevTimestamp == null) {
                        this.subGiftHandleTimestamps.put(subGiftId, now);
                    } else if (prevTimestamp + 5000L <= now) {
                        TwitchSpawn.LOGGER.warn("Sub gift was already handled less than 5 seconds ago. Skipping -> {}", message);
                        return;
                    }
                }
            }

            // Fetch the appropriate builder
            EventBuilder eventBuilder = TSLEventKeyword.getBuilder(eventPair);

            // Unregistered event alias
            if (eventBuilder == null)
                return; // Stop here, do not handle

            // Build arguments
            EventArguments eventArguments = eventBuilder.build(Minecraft.getInstance().player.getDisplayName().getString(), eventPair,
                    message, Platform.STREAMLABS);

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
