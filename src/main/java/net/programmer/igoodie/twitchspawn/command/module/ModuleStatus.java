package net.programmer.igoodie.twitchspawn.command.module;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentTranslation;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;

public class ModuleStatus extends CommandModule {

    @Override
    public String getName() {
        return "status";
    }

    @Override
    public void execute(ICommandSender commandSender, String[] moduleArgs) throws CommandException {
        String translationKey = TwitchSpawn.TRACE_MANAGER.isRunning() ?
                "commands.twitchspawn.status.on" : "commands.twitchspawn.status.off";

        commandSender.sendMessage(new TextComponentTranslation(translationKey));
    }

}
