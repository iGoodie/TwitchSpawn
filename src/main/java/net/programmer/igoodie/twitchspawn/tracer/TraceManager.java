package net.programmer.igoodie.twitchspawn.tracer;

import net.minecraft.command.CommandSource;
import net.minecraft.util.text.TranslationTextComponent;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;

public class TraceManager {

    private boolean running;
    private StreamlabsSocketTracer streamlabsSocketTracer;

    public TraceManager() {
        this.streamlabsSocketTracer = new StreamlabsSocketTracer(this);
    }

    public boolean isRunning() {
        return running;
    }

    public void start() {
        TwitchSpawn.LOGGER.info("Starting all the tracers...");

        // Start tracers
        streamlabsSocketTracer.start();

        TwitchSpawn.SERVER.getPlayerList().sendMessage(
                new TranslationTextComponent("commands.twitchspawn.start.success"), true);
    }

    public void stop(CommandSource source, String reason) {
        TwitchSpawn.LOGGER.info("Stopping all the tracers...");

        // Stop tracers
        streamlabsSocketTracer.stop();

        if (TwitchSpawn.SERVER != null) {
            TwitchSpawn.SERVER.getPlayerList().sendMessage(
                    new TranslationTextComponent("commands.twitchspawn.stop.success",
                            source == null ? "Server" : source.getName(), reason), true);
        }
    }

}
