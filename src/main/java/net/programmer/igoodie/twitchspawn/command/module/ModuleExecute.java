package net.programmer.igoodie.twitchspawn.command.module;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentTranslation;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.action.TSLAction;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLTokenizer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ModuleExecute extends CommandModule {

    @Override
    public String getName() {
        return "execute";
    }

    @Override
    public String getUsage() {
        return super.getUsage() + " <tsl_action>";
    }

    @Override
    public void execute(ICommandSender commandSender, String[] moduleArgs) throws CommandException {
        String senderNickname = commandSender.getName();
        if (!ConfigManager.CREDENTIALS.hasPermission(senderNickname)) {
            commandSender.sendMessage(new TextComponentTranslation("commands.twitchspawn.execute.no_perm"));
            TwitchSpawn.LOGGER.info("{} tried to execute an action, but no permission", senderNickname);
            return;
        }
        try { 
            TSLAction tslAction = parseAction(moduleArgs);
            EventArguments eventArguments = EventArguments.createRandom(commandSender.getName());
            tslAction.process(eventArguments);

        } catch (TSLSyntaxError e) {
            throw new CommandException(e.getMessage());
        }
    }

    private TSLAction parseAction(String[] moduleArgs) throws CommandException, TSLSyntaxError {
        if (moduleArgs.length == 0)
            throw new CommandException("Expected at least 1 TSL word!");

        List<String> words = TSLTokenizer.intoWords(String.join(" ", moduleArgs));
        String actionName = words.remove(0);

        return TSLParser.parseAction(actionName, words);
    }

}
