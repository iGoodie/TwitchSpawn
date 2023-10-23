package net.programmer.igoodie.twitchspawn.tracer;

import io.socket.client.IO;
import io.socket.client.Socket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.configuration.CredentialsConfig;
import net.programmer.igoodie.twitchspawn.network.NetworkManager;
import net.programmer.igoodie.twitchspawn.network.packet.StatusChangedPacket;
import net.programmer.igoodie.twitchspawn.tracer.chat.TwitchChatTracer;
import net.programmer.igoodie.twitchspawn.tracer.socket.StreamElementsSocketTracer;
import net.programmer.igoodie.twitchspawn.tracer.socket.StreamlabsSocketTracer;
import net.programmer.igoodie.twitchspawn.tracer.socket.TwitchPubSubTracer;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class TraceManager {

    private boolean running;
    private Map<String, Socket> sockets; // mc_nick.lowercase() -> sio_socket
    private List<WebSocketTracer> webSocketTracers;

    public TraceManager() {
        this.sockets = new HashMap<>();
        this.webSocketTracers = new LinkedList<>();
    }

    public boolean isRunning() {
        synchronized (this) {
            return running;
        }
    }

    public void start() {
        if (isRunning()) throw new IllegalStateException("Tracer is already started");

        TwitchSpawn.LOGGER.info("Starting all the tracers...");

        running = true;

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(100);
        dispatcher.setMaxRequestsPerHost(100);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();

        IO.setDefaultOkHttpWebSocketFactory(okHttpClient);
        IO.setDefaultOkHttpCallFactory(okHttpClient);

        // Start Websocket tracers
        this.webSocketTracers.add(new TwitchPubSubTracer(this)); // TODO: Extract to a worker, not master
        this.webSocketTracers.add(new TwitchChatTracer(this)); // TODO: Extract to a worker, not master
        webSocketTracers.forEach(WebSocketTracer::start);

        // Connect online players from credentials.toml
        for (CredentialsConfig.Streamer streamer : ConfigManager.CREDENTIALS.streamers) {
            if (TwitchSpawn.SERVER.getPlayerList().getPlayerByName(streamer.minecraftNick) != null) {
                connectStreamer(streamer.minecraftNick);
            }
        }

        for (ServerPlayer player : TwitchSpawn.SERVER.getPlayerList().getPlayers()) {
            UUID uuid = player.getUUID();
            TranslatableComponent successText = new TranslatableComponent("commands.twitchspawn.start.success");
            player.sendMessage(successText, uuid);
            NetworkManager.CHANNEL.sendTo(new StatusChangedPacket(true),
                    player.connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT);
        }
    }

    public void stop(CommandSourceStack source, String reason) {
        if (!isRunning()) throw new IllegalStateException("Tracer is already stopped");

        TwitchSpawn.LOGGER.info("Stopping all the tracers...");

        running = false;

        // Stop Websocket tracers and reset the list
        webSocketTracers.forEach(WebSocketTracer::stop);
        webSocketTracers.clear();

        // Disconnect each alive socket and reset the map
        for (Socket socket : sockets.values()) {
            socket.disconnect();
        }
        sockets.clear();

        if (TwitchSpawn.SERVER != null) {
            for (ServerPlayer player : TwitchSpawn.SERVER.getPlayerList().getPlayers()) {
                UUID uuid = player.getUUID();
                TranslatableComponent successText = new TranslatableComponent("commands.twitchspawn.stop.success",
                        source == null ? "Server" : source.getTextName(), reason);
                player.sendMessage(successText, uuid);
                NetworkManager.CHANNEL.sendTo(new StatusChangedPacket(false),
                        player.connection.connection,
                        NetworkDirection.PLAY_TO_CLIENT);
            }
        }
    }

    public void connectStreamer(String nickname) {
        if (!isRunning()) throw new IllegalStateException("Cannot connect streamer when TwitchSpawn is stopped.");

        CredentialsConfig.Streamer streamerConfig = ConfigManager.CREDENTIALS.streamers.stream()
                .filter(streamer -> streamer.minecraftNick.equalsIgnoreCase(nickname))
                .findFirst().orElse(null);

        if (streamerConfig == null) {
            TwitchSpawn.LOGGER.warn("{} is not set as a Streamer in credentials.toml. Skipping connection for them.", nickname);
            return;
        }

        SocketIOTracer tracer;

        if (streamerConfig.platform == Platform.STREAMLABS) {
            tracer = new StreamlabsSocketTracer(this);

        } else if (streamerConfig.platform == Platform.STREAMELEMENTS) {
            tracer = new StreamElementsSocketTracer(this);

        } else {
            // How is this even possible?
            throw new InternalError("TODOTODOOOO");
        }

        Socket socket = tracer.createSocket(streamerConfig);
        sockets.put(nickname.toLowerCase(), socket);
        socket.connect();
    }

    public void disconnectStreamer(String nickname) {
        if (!isRunning()) throw new IllegalStateException("Cannot disconnect streamer when TwitchSpawn is stopped.");

        Socket socket = sockets.get(nickname.toLowerCase());

        if (socket == null) {
            TwitchSpawn.LOGGER.warn("{} is not set as a Streamer in credentials.toml. Skipping disconnection for them.", nickname);
            return;
        }

        socket.disconnect();
        sockets.remove(nickname.toLowerCase());
    }

}
