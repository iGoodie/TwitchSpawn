package net.programmer.igoodie.twitchspawn.command.module;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentTranslation;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.command.TwitchSpawnCommand;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;

public class ModuleQuickRefresh extends CommandModule {

    private TwitchSpawnCommand command;

    public ModuleQuickRefresh(TwitchSpawnCommand command) {
        this.command = command;
    }

    @Override
    public String getName() {
        return "quickrefresh";
    }

    @Override
    public void execute(ICommandSender commandSender, String[] moduleArgs) throws CommandException {
        String senderNickname = commandSender.getName();

        if (!ConfigManager.CREDENTIALS.hasPermission(senderNickname)) {
            commandSender.sendMessage(new TextComponentTranslation("commands.twitchspawn.reloadcfg.no_perm"));
            TwitchSpawn.LOGGER.info("{} tried to run TwitchSpawn, but no permission", senderNickname);
            return;
        }

        if (TwitchSpawn.TRACE_MANAGER.isRunning()) {
            TwitchSpawn.TRACE_MANAGER.stop(commandSender, "Quick refreshing");
        }

        command.modules.get("reloadcfg").execute(commandSender, moduleArgs);
        command.modules.get("start").execute(commandSender, moduleArgs);
    }

}
