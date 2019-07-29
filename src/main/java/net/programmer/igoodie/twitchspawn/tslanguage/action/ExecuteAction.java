package net.programmer.igoodie.twitchspawn.tslanguage.action;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

import java.util.List;

public class ExecuteAction extends TSLAction {

    private List<String> commands;

    public ExecuteAction(List<String> words) throws TSLSyntaxError {
        if (words.size() == 0)
            throw new TSLSyntaxError("Expected at least one command.");

        if (!words.stream().allMatch(word -> word.startsWith("/")))
            throw new TSLSyntaxError("Every command must start with '/' character");

        this.commands = words;
    }

    @Override
    protected void performAction(ServerPlayerEntity player) {
        CommandSource source = player.getCommandSource()
                .withPermissionLevel(9999) // OVER 9000!
                .withFeedbackDisabled();

        commands.forEach(command -> {
            int result = player.getServer().getCommandManager().handleCommand(source, command);
            TwitchSpawn.LOGGER.info("Executed (Status:{}) -> {}", result, command);
        });
    }

    @Override
    protected String subtitleEvaluator(String expression, EventArguments args) {
        return null; // No extra evaluation
    }

}
