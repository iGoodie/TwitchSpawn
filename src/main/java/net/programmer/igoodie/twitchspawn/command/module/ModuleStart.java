package net.programmer.igoodie.twitchspawn.command.module;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentTranslation;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;

public class ModuleStart extends CommandModule {

    @Override
    public String getName() {
        return "start";
    }

    @Override
    public void execute(ICommandSender commandSender, String[] moduleArgs) throws CommandException {
        String senderNickname = commandSender.getName();

        // Sender is not permitted to start tracers
        if (!ConfigManager.CREDENTIALS.hasPermission(senderNickname)) {
            commandSender.sendMessage(new TextComponentTranslation("commands.twitchspawn.start.no_perm"));
            TwitchSpawn.LOGGER.info("{} tried to run TwitchSpawn, but no permission", senderNickname);
            return;
        }

        // Try starting tracers
        try { TwitchSpawn.TRACE_MANAGER.start(); } catch (IllegalStateException e) {
            commandSender.sendMessage(new TextComponentTranslation("commands.twitchspawn.start.illegal_state"));
        }
    }

}
