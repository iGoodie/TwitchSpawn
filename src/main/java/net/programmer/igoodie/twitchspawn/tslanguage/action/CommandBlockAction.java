package net.programmer.igoodie.twitchspawn.tslanguage.action;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;

import java.util.List;

public class CommandBlockAction extends TSLAction {

    String command;

    public CommandBlockAction(List<String> args) {
        // TODO
        command = args.get(0);
    }

    @Override
    protected void performAction(ServerPlayerEntity player) {
        // TODO Disable logging to OP users?

        CommandSource source = player.getCommandSource().withPermissionLevel(10);

        TwitchSpawn.SERVER.getCommandManager().handleCommand(source, command);
    }

    @Override
    protected String subtitleEvaluator(String expression, EventArguments args) {
        return null;
    }

}
