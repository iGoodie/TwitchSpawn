package net.programmer.igoodie.twitchspawn.tslanguage.action;

import net.minecraft.server.permissions.LevelBasedPermissionSet;
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
        for (String command : commands) {
            TwitchSpawn.SERVER.execute(() -> {
                // Commands no longer return their result directly, capture it through the callback
                int[] resultHolder = {0};
                CommandSourceStack source = player.createCommandSourceStack()
                        .withPermission(LevelBasedPermissionSet.OWNER) // OVER 9000!
                        .withSuppressedOutput()
                        .withCallback((success, value) -> resultHolder[0] = success ? value : 0);

                TwitchSpawn.SERVER
                        .getCommands()
                        .performPrefixedCommand(source, replaceExpressions(command, args));
                int result = resultHolder[0];

                if (result <= 0) { // Wohooo we knew iGoodie liked hacky solutions. ( ? :/ )
                    // If it yielded an error, and not worked as expected
                    // Then turn on the feedback, and run it again! Brilliant! What could go wrong? :))))))
                    CommandSourceStack newSource = player.createCommandSourceStack()
                            .withPermission(LevelBasedPermissionSet.OWNER);
                    TwitchSpawn.SERVER
                            .getCommands()
                            .performPrefixedCommand(newSource, replaceExpressions(command, args));
                }

                TwitchSpawn.LOGGER.info("Executed (Status:{}) -> {}", result, replaceExpressions(command, args));
            });
        }
    }

}
