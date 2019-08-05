package net.programmer.igoodie.twitchspawn.tracer;

import io.socket.client.IO;
import io.socket.client.Socket;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.TranslationTextComponent;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.configuration.CredentialsConfig;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEvent;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEventPair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class StreamlabsSocketClient {

    private static StreamlabsSocketClient instance;

    public static StreamlabsSocketClient getInstance() {
        return instance;
    }

    public static void start() {
        if (instance != null)
            throw new IllegalStateException("Streamlabs socket is already listening");

        TwitchSpawn.LOGGER.info("Starting Streamlabs client...");

        instance = new StreamlabsSocketClient();
        instance.sockets.forEach(s -> s.connect());

        TwitchSpawn.SERVER.getPlayerList().sendMessage(
                new TranslationTextComponent("commands.twitchspawn.start.success"), true);

        TwitchSpawn.LOGGER.info("Started Streamlabs client");
    }

    public static void stop(CommandSource source, String reason) {
        if (instance == null)
            throw new IllegalStateException("Streamlabs socket is already stopped");

        TwitchSpawn.LOGGER.info("Stopping Streamlabs client...");

        instance.sockets.forEach(s -> s.disconnect());
        instance = null;

        if (TwitchSpawn.SERVER != null) {
            TwitchSpawn.SERVER.getPlayerList().sendMessage(
                    new TranslationTextComponent("commands.twitchspawn.stop.success",
                            source == null ? "Server" : source.getName(), reason), true);
        }

        TwitchSpawn.LOGGER.info("Stopped Streamlabs client {}",
                reason == null ? "" : String.format("(Reason: %s)", reason));
    }

    public static boolean isRunning() {
        return instance != null;
    }

    /* --------------------------------------------------- */

    private List<Socket> sockets = new LinkedList<>();

    private StreamlabsSocketClient() {
        ConfigManager.CREDENTIALS.streamers.forEach(this::createSocket);
    }

    private void createSocket(CredentialsConfig.Streamer streamer) {
        try {
            if (streamer.socketToken == null || streamer.socketToken.isEmpty())
                throw new IllegalArgumentException("Socket token is not set.");

            IO.Options options = new IO.Options();
            options.forceNew = true;
            options.reconnection = false;
            options.transports = new String[]{"websocket"};
            options.query = "token=" + streamer.socketToken;

            Socket socket = IO.socket("https://sockets.streamlabs.com", options);

            AtomicBoolean authorized = new AtomicBoolean(false); // Weird trick

            // Socket connected to the server with no rejections
            socket.on(Socket.EVENT_CONNECT, obj -> {
                TwitchSpawn.LOGGER.info("Connected to Streamlabs Socket with {}'s socket token successfully!", streamer.minecraftNick);
                authorized.set(true);
            });

            // Socket disconnected before or after connection
            socket.on(Socket.EVENT_DISCONNECT, obj -> {
                TwitchSpawn.LOGGER.info("Disconnected from {}'s Streamlabs socket connection. ({})",
                        streamer.minecraftNick, authorized.get() ? "intentional" : "unauthorized");

                if (authorized.get() == false) {
                    StreamlabsSocketClient.stop(null, streamer.twitchNick + " unauthorized by the socket server");
                }
            });

            // Socket received a live event
            socket.on("event", args -> onEvent(socket, streamer, args));

            this.sockets.add(socket);


        } catch (URISyntaxException e) {
            throw new InternalError("Invalid URL. TwitchSpawn is outdated, probably Streamlabs URL is changed.");
        }
    }

    private void onEvent(Socket socket, CredentialsConfig.Streamer streamer, Object... args) {
        JSONObject response = (JSONObject) args[0];

        if (!response.has("message") || response.optJSONArray("message") == null)
            return; // No message field (in expected format), stop here

        String responseType = extractFrom(response, "type", String.class, null);
        String responseFor = extractFrom(response, "for", String.class, null);

        JSONArray messages = extractFrom(response, "message", JSONArray.class, null);

        forEachMessage(messages, message -> {
            TwitchSpawn.LOGGER.info("Received streamlabs package {} -> {}",
                    new TSLEventPair(responseType, responseFor), message);

            // Unregistered event alias
            if (TSLEvent.getEventAlias(responseType, responseFor) == null)
                return; // Stop here, do not handle

            // Refine incoming data into EventArguments model
            EventArguments eventArguments = new EventArguments(responseType, responseFor);
            eventArguments.streamerNickname = streamer.minecraftNick;
            eventArguments.actorNickname = extractFrom(message, "name", String.class, null);
            eventArguments.message = extractFrom(message, "message", String.class, null);
            eventArguments.donationAmount = extractNumberFrom(message, "amount", 0.0).doubleValue();
            eventArguments.donationCurrency = extractFrom(message, "currency", String.class, null);
            eventArguments.subscriptionMonths = extractNumberFrom(message, "months", 0).intValue();
            eventArguments.raiderCount = extractNumberFrom(message, "raiders", 0).intValue();
            eventArguments.viewerCount = extractNumberFrom(message, "viewers", 0).intValue();

            // Pass the model to the handler
            ConfigManager.RULESET_COLLECTION.handleEvent(eventArguments);
        });
    }

    /**
     * Here to handle unpredictable value type
     * send by Streamlabs Socket API.
     * (E.g the type of "amount" is String, Integer or Double)
     */
    private Number extractNumberFrom(JSONObject json, String key, Number defaultValue) {
        Object value = null;

        try {
            value = json.get(key);

            if (value instanceof String)
                return Double.parseDouble((String) value);

            return (Number) value;

        } catch (JSONException e) {
            return defaultValue;
        } catch (NumberFormatException e) {
            throw new InternalError("That is bad.. Streamlabs Socket API sent malformed number. -> " + value);
        }
    }

    private <T> T extractFrom(JSONObject json, String key, Class<T> type, T defaultValue) {
        try {
            Object value = json.get(key);
            return type.cast(value);

        } catch (JSONException e) {
            return defaultValue;

        } catch (ClassCastException e) {
            throw new InternalError(String.format("Unable to cast %s key into %s", key, type.getSimpleName()));
        }
    }

    private void forEachMessage(JSONArray array, Consumer<JSONObject> consumer) {
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject json = array.getJSONObject(i);
                consumer.accept(json);

            } catch (JSONException e) {
                throw new InternalError("Error performing JSONArray forEachMessage.");
            }
        }
    }

}
