package net.programmer.igoodie.twitchspawn.command.module;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.text.TextComponentTranslation;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.tslanguage.TSLRuleset;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class ModuleRules extends CommandModule {

    @Override
    public String getName() {
        return "rules";
    }

    @Override
    public String getUsage() {
        return super.getUsage() + " [<streamer_nick>|default]";
    }

    @Nonnull
    @Override
    public List<String> getTabCompletions(String[] moduleArgs) {
        if (moduleArgs.length == 1)
            return listOfCompletionsStartingWith(moduleArgs,
                    "default", ConfigManager.RULESET_COLLECTION.getStreamers());

        return Collections.emptyList();
    }

    @Override
    public void execute(ICommandSender commandSender, String[] moduleArgs) throws CommandException {
        if (moduleArgs.length > 1)
            throw new WrongUsageException(getUsage());

        String rulesetName = getArgument(moduleArgs, 0);

        // No ruleset name is provided
        if (rulesetName == null) {
            commandSender.sendMessage(new TextComponentTranslation("commands.twitchspawn.rules.list",
                    ConfigManager.RULESET_COLLECTION.getStreamers()));
            return;
        }

        // Fetch ruleset with given name
        TSLRuleset ruleset = ConfigManager.RULESET_COLLECTION.getRuleset(rulesetName);

        // No associated ruleset was found
        if (ruleset == null) {
            commandSender.sendMessage(new TextComponentTranslation("commands.twitchspawn.rules.one.fail", rulesetName));
            return;
        }

        // Feedback with rules
        String translationKey = rulesetName.equalsIgnoreCase("default")
                ? "commands.twitchspawn.rules.default"
                : "commands.twitchspawn.rules.one";
        commandSender.sendMessage(new TextComponentTranslation(translationKey, rulesetName, "\n" + ruleset.toString()));
    }

}
