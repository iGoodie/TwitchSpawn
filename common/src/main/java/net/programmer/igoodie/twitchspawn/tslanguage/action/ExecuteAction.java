package net.programmer.igoodie.twitchspawn.tslanguage.action;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
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

//        if (!actionWords.stream().allMatch(word -> word.startsWith("/")))
//            throw new TSLSyntaxError("Every command must start with '/' character");

        this.commands = new LinkedList<>(actionWords);
    }

    @Override
    protected void performAction(ServerPlayer player, EventArguments args) {
        CommandSourceStack source = player.createCommandSourceStack()
                .withPermission(9999) // OVER 9000!
                .withSuppressedOutput();


        for (String command : commands) {
            TwitchSpawn.SERVER.execute(() -> {
                int result = TwitchSpawn.SERVER
                    .getCommands()
                    .performPrefixedCommand(source, replaceExpressions(command, args));

                if (result <= 0) { // Wohooo we knew iGoodie liked hacky solutions. ( ? :/ )
                    // If it yielded an error, and not worked as expected
                    // Then turn on the feedback, and run it again! Brilliant! What could go wrong? :))))))
                    CommandSourceStack newSource = player.createCommandSourceStack()
                            .withPermission(9999);
                    TwitchSpawn.SERVER
                        .getCommands()
                        .performPrefixedCommand(newSource, replaceExpressions(command, args));
                }

                TwitchSpawn.LOGGER.info("Executed (Status:{}) -> {}", result, replaceExpressions(command, args));
            });
        }
    }

}
