package net.programmer.igoodie.twitchspawn.tracer;

import net.minecraft.command.CommandSource;
import net.minecraft.util.text.TranslationTextComponent;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;

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
        }
    }

}
