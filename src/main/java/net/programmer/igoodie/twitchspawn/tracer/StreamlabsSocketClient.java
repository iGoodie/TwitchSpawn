package net.programmer.igoodie.twitchspawn.tracer;

import io.socket.client.IO;
import io.socket.client.Socket;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.configuration.CredentialsConfig;

import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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

        TwitchSpawn.LOGGER.info("Started Streamlabs client");
    }

    public static void stop() { stop(null); }

    public static void stop(String reason) {
        if (instance == null)
            throw new IllegalStateException("Streamlabs socket is already stopped");

        TwitchSpawn.LOGGER.info("Stopping Streamlabs client...");

        instance.sockets.forEach(s -> s.disconnect());
        instance = null;

        TwitchSpawn.LOGGER.info("Stopped Streamlabs client {}",
                reason == null ? "" : String.format("(Reason: %s)", reason));
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
                    StreamlabsSocketClient.stop("Unauthorized by the socket server");
                }
            });

            // Socket received a live event
            socket.on("event", args -> onEvent(socket, streamer, args));

            this.sockets.add(socket);


        } catch (URISyntaxException e) {
            throw new InternalError("Invalid URL. TwitchSpawn is outdated, probably Streamlabs URL is changed.");
        }
    }

    private void onEvent(Socket socket, CredentialsConfig.Streamer streamer, Object...args) {
        // TODO
    }

}
