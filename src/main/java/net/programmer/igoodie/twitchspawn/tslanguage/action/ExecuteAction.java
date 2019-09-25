package net.programmer.igoodie.twitchspawn.tslanguage.action;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.FutureTask;

public class ExecuteAction extends TSLAction {

    private List<String> commands;

    public ExecuteAction(List<String> words) throws TSLSyntaxError {
        this.message = TSLParser.parseMessage(words);
        List<String> actionWords = actionPart(words);

        if (actionWords.size() == 0)
            throw new TSLSyntaxError("Expected at least one command.");

        if (!actionWords.stream().allMatch(word -> word.startsWith("/")))
            throw new TSLSyntaxError("Every command must start with '/' character");

        this.commands = new LinkedList<>(words);
    }

    @Override
    protected void performAction(EntityPlayerMP player, EventArguments args) {
        ICommandSender source = player.getCommandSenderEntity();
//                .withPermissionLevel(9999) // OVER 9000!
//                .withFeedbackDisabled();


        for (String command : commands) {
            TwitchSpawn.SERVER.futureTaskQueue.add(new FutureTask<>(() -> {
                int result = TwitchSpawn.SERVER
                        .getCommandManager()
                        .executeCommand(source, replaceExpressions(command, args));

                TwitchSpawn.LOGGER.info("Executed (Status:{}) -> {}", result, replaceExpressions(command, args));
            }, "Test response")); // TODO
        }
    }

}
