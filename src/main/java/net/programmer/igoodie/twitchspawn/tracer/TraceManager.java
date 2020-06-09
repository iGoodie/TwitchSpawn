package net.programmer.igoodie.twitchspawn.tracer;

import io.socket.client.Socket;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkDirection;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.configuration.CredentialsConfig;
import net.programmer.igoodie.twitchspawn.network.NetworkManager;
import net.programmer.igoodie.twitchspawn.network.packet.StatusChangedPacket;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TraceManager {

    private boolean running;
    private Map<String, Socket> sockets; // mc_nick.lowercase() -> sio_socket
    private List<WebSocketTracer> webSocketTracers;

    public TraceManager() {
        this.sockets = new HashMap<>();

        this.webSocketTracers = new LinkedList<>();
        this.webSocketTracers.add(new TwitchPubSubTracer(this));
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

        // Start Websocket tracers
        webSocketTracers.forEach(WebSocketTracer::start);

        // Connect online players from credentials.toml
        for (CredentialsConfig.Streamer streamer : ConfigManager.CREDENTIALS.streamers) {
            if (TwitchSpawn.SERVER.getPlayerList().getPlayerByUsername(streamer.minecraftNick) != null) {
                connectStreamer(streamer.minecraftNick);
            }
        }

        TwitchSpawn.SERVER.getPlayerList().sendMessage(
                new TranslationTextComponent("commands.twitchspawn.start.success"), true);
        TwitchSpawn.SERVER.getPlayerList().getPlayers().forEach(player -> {
            NetworkManager.CHANNEL.sendTo(new StatusChangedPacket(true),
                    player.connection.netManager,
                    NetworkDirection.PLAY_TO_CLIENT);
        });
    }

    public void stop(CommandSource source, String reason) {
        if (!isRunning()) throw new IllegalStateException("Tracer is already stopped");

        TwitchSpawn.LOGGER.info("Stopping all the tracers...");

        running = false;

        // Stop Websocket tracers
        webSocketTracers.forEach(WebSocketTracer::stop);

        // Disconnect each alive socket and reset the map
        for (Socket socket : sockets.values()) {
            socket.disconnect();
        }
        sockets.clear();

        if (TwitchSpawn.SERVER != null) {
            TwitchSpawn.SERVER.getPlayerList().sendMessage(
                    new TranslationTextComponent("commands.twitchspawn.stop.success",
                            source == null ? "Server" : source.getName(), reason));
            TwitchSpawn.SERVER.getPlayerList().getPlayers().forEach(player -> {
                NetworkManager.CHANNEL.sendTo(new StatusChangedPacket(false),
                        player.connection.netManager,
                        NetworkDirection.PLAY_TO_CLIENT);
            });
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
