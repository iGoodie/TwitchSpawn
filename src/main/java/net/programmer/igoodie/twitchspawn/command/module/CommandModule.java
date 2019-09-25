package net.programmer.igoodie.twitchspawn.command.module;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.programmer.igoodie.twitchspawn.command.TwitchSpawnCommand;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public abstract class CommandModule {

    public abstract String getName();

    public String getUsage() {
        return "/twitchspawn " + getName();
    }

    public @Nonnull List<String> getTabCompletions(String[] moduleArgs) {
        return Collections.emptyList();
    }

    public abstract void execute(ICommandSender commandSender, String[] moduleArgs) throws CommandException;

}
