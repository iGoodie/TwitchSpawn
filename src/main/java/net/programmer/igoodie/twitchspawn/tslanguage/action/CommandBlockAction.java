package net.programmer.igoodie.twitchspawn.tslanguage.action;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

import java.util.List;

public class CommandBlockAction extends TSLAction {

    private List<String> commands;

    public CommandBlockAction(List<String> args) throws TSLSyntaxError {
        if (!args.stream().allMatch(arg -> arg.startsWith("/")))
            throw new TSLSyntaxError("Every command must start with '/' character");

        this.commands = args;
    }

    @Override
    protected void performAction(ServerPlayerEntity player) {
        CommandSource source = player.getCommandSource()
                .withPermissionLevel(9999) // OVER 9000!
                .withFeedbackDisabled();

        commands.forEach(command -> {
            int result = TwitchSpawn.SERVER.getCommandManager().handleCommand(source, command);
            TwitchSpawn.LOGGER.info("Executed (Status:{}) -> {}", result, command);
        });
    }

    @Override
    protected String subtitleEvaluator(String expression, EventArguments args) {
        return null; // No extra evaluation
    }

}
