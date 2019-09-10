package net.programmer.igoodie.twitchspawn.tracer;

import net.minecraft.command.CommandSource;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkDirection;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.network.NetworkManager;
import net.programmer.igoodie.twitchspawn.network.packet.StatusChangedPacket;

import java.util.LinkedList;
import java.util.List;

public class TraceManager {

    private boolean running;
    private List<SocketIOTracer> socketIOTracers;

    public TraceManager() {
        this.socketIOTracers = new LinkedList<>();
        this.socketIOTracers.add(new StreamlabsSocketTracer(this));
        this.socketIOTracers.add(new StreamElementsSocketTracer(this));
    }

    public boolean isRunning() {
        synchronized (this) {
            return running;
        }
    }

    public void start() {
        if (isRunning()) throw new IllegalStateException("Tracer is already started");

        TwitchSpawn.LOGGER.info("Starting all the tracers...");

        // Start tracers
        socketIOTracers.forEach(SocketIOTracer::start);

        running = true;

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

        // Stop tracers
        socketIOTracers.forEach(SocketIOTracer::stop);

        running = false;

        if (TwitchSpawn.SERVER != null) {
            TwitchSpawn.SERVER.getPlayerList().sendMessage(
                    new TranslationTextComponent("commands.twitchspawn.stop.success",
                            source == null ? "Server" : source.getName(), reason), true);
            TwitchSpawn.SERVER.getPlayerList().getPlayers().forEach(player -> {
                NetworkManager.CHANNEL.sendTo(new StatusChangedPacket(false),
                        player.connection.netManager,
                        NetworkDirection.PLAY_TO_CLIENT);
            });
        }
    }

}
