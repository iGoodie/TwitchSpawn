package net.programmer.igoodie.twitchspawn.command.module;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentTranslation;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.TwitchSpawnLoadingErrors;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;

import java.util.stream.Stream;

public class ModuleReloadcfg extends CommandModule {

    @Override
    public String getName() {
        return "reloadcfg";
    }

    @Override
    public void execute(ICommandSender commandSender, String[] moduleArgs) throws CommandException {
        String senderNickname = commandSender.getName();

        boolean isOp = TwitchSpawn.SERVER.isSinglePlayer()
                || Stream.of(TwitchSpawn.SERVER.getPlayerList().getOppedPlayerNames())
                .anyMatch(oppedPlayerName -> oppedPlayerName.equalsIgnoreCase(senderNickname));

        // If is not OP or has no permission
        if (!isOp && !ConfigManager.CREDENTIALS.hasPermission(senderNickname)) {
            commandSender.sendMessage(new TextComponentTranslation("commands.twitchspawn.reloadcfg.no_perm"));
            TwitchSpawn.LOGGER.info("{} tried to reload TwitchSpawn configs, but no permission", senderNickname);
            return;
        }

        if (TwitchSpawn.TRACE_MANAGER.isRunning()) {
            commandSender.sendMessage(new TextComponentTranslation("commands.twitchspawn.reloadcfg.already_started"));
            return;
        }

        try {
            ConfigManager.loadConfigs();
            commandSender.sendMessage(new TextComponentTranslation("commands.twitchspawn.reloadcfg.success"));

        } catch (TwitchSpawnLoadingErrors e) {
            String errorLog = "\n• " + e.toString().replace("\n", "\n• ");
            commandSender.sendMessage(new TextComponentTranslation("commands.twitchspawn.reloadcfg.invalid_syntax", errorLog));
        }
    }

}
