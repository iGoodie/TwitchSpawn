package net.programmer.igoodie.twitchspawn.command.module;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class CommandModule {

    public abstract String getName();

    public String getUsage() {
        return "/twitchspawn " + getName();
    }

    public @Nonnull
    List<String> getTabCompletions(String[] moduleArgs) {
        return Collections.emptyList();
    }

    public abstract void execute(ICommandSender commandSender, String[] moduleArgs) throws CommandException;

    protected String getArgument(String[] moduleArgs, int index) {
        return getArgument(moduleArgs, index, null);
    }

    protected String getArgument(String[] moduleArgs, int index, String defaultArgument) {
        return (0 <= index && index < moduleArgs.length)
                ? moduleArgs[index] : defaultArgument;
    }

    protected List<String> listOfCompletionsStartingWith(String[] moduleArgs, Object... possibleCompletions) {
        if (moduleArgs.length == 0)
            return Collections.emptyList();

        String lastArgument = moduleArgs[moduleArgs.length - 1];
        List<String> completions = new LinkedList<>();

        for (Object possibleCompletion : possibleCompletions) {
            if (possibleCompletion instanceof String) {
                includeIfStartsWith(completions, lastArgument, possibleCompletion);
            }

            if (possibleCompletion instanceof Iterable) {
                ((Iterable) possibleCompletion).forEach(possibleObject -> {
                    includeIfStartsWith(completions, lastArgument, possibleObject);
                });
            }
        }

        return completions;
    }

    private void includeIfStartsWith(List<String> completions, String lastWord, Object possibleObject) {
        if (!(possibleObject instanceof String))
            return;

        String possibleString = (String) possibleObject;

        if (possibleString.toLowerCase().startsWith(lastWord.toLowerCase()))
            completions.add(possibleString);
    }

}
