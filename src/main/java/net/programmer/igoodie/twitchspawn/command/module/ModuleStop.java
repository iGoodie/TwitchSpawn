package net.programmer.igoodie.twitchspawn.command.module;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentTranslation;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;

public class ModuleStop extends CommandModule {

    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public void execute(ICommandSender commandSender, String[] moduleArgs) throws CommandException {
        String sourceNickname = commandSender.getName();

        // If has no permission
        if (!ConfigManager.CREDENTIALS.hasPermission(sourceNickname)) {
            commandSender.sendMessage(new TextComponentTranslation("commands.twitchspawn.stop.no_perm"));
            TwitchSpawn.LOGGER.info("{} tried to stop TwitchSpawn, but no permission", sourceNickname);
            return;
        }

        try {
            TwitchSpawn.TRACE_MANAGER.stop(commandSender, "Command execution");

        } catch (IllegalStateException e) {
            commandSender.sendMessage(new TextComponentTranslation("commands.twitchspawn.stop.illegal_state"));
        }
    }

}
