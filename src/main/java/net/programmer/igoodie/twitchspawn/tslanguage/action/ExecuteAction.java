package net.programmer.igoodie.twitchspawn.tslanguage.action;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

import java.util.LinkedList;
import java.util.List;

public class ExecuteAction extends TSLAction {

    private List<String> commands;

    public ExecuteAction(List<String> words) throws TSLSyntaxError {
        this.message = TSLParser.parseMessage(words);
        List<String> actionWords = actionPart(words);

        if (actionWords.size() == 0)
            throw new TSLSyntaxError("Expected at least one command.");

        if (!actionWords.stream().allMatch(word -> word.startsWith("/")))
            throw new TSLSyntaxError("Every command must start with '/' character");

        this.commands = new LinkedList<>(actionWords);
    }

    @Override
    protected void performAction(ServerPlayerEntity player, EventArguments args) {
        CommandSource source = player.getCommandSource()
                .withPermissionLevel(9999) // OVER 9000!
                .withFeedbackDisabled();


        for (String command : commands) {
            TwitchSpawn.SERVER.execute(() -> {
                int result = TwitchSpawn.SERVER
                        .getCommandManager()
                        .handleCommand(source, replaceExpressions(command, args));

                if (result <= 0) { // Wohooo we knew iGoodie liked hacky solutions. ( ? :/ )
                    // If it yielded an error, and not worked as expected
                    // Then turn on the feedback, and run it again! Brilliant! What could go wrong? :))))))
                    CommandSource newSource = player.getCommandSource()
                            .withPermissionLevel(9999);
                    TwitchSpawn.SERVER
                            .getCommandManager()
                            .handleCommand(newSource, replaceExpressions(command, args));
                }

                TwitchSpawn.LOGGER.info("Executed (Status:{}) -> {}", result, replaceExpressions(command, args));
            });
        }
    }

}
