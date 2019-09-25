package net.programmer.igoodie.twitchspawn.tracer;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentTranslation;
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
                new TextComponentTranslation("commands.twitchspawn.start.success"), true);
        TwitchSpawn.SERVER.getPlayerList().getPlayers().forEach(player -> {
            NetworkManager.CHANNEL.sendTo(
                    new StatusChangedPacket.Message(true),
                    player
            );
        });
    }

    public void stop(ICommandSender sender, String reason) {
        if (!isRunning()) throw new IllegalStateException("Tracer is already stopped");

        TwitchSpawn.LOGGER.info("Stopping all the tracers...");

        // Stop tracers
        socketIOTracers.forEach(SocketIOTracer::stop);

        running = false;

        if (TwitchSpawn.SERVER != null) {
            TwitchSpawn.SERVER.getPlayerList().sendMessage(
                    new TextComponentTranslation("commands.twitchspawn.stop.success",
                            sender == null ? "Server" : sender.getName(), reason));
            TwitchSpawn.SERVER.getPlayerList().getPlayers().forEach(player -> {
                NetworkManager.CHANNEL.sendTo(
                        new StatusChangedPacket.Message(false),
                        player
                );
            });
        }
    }

}
