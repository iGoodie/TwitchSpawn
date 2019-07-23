package net.programmer.igoodie.twitchspawn.tslanguage.action;

import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.List;

public class CommandBlockAction extends TSLAction {

    String command;

    public CommandBlockAction(List<String> args) {
        // TODO
    }

    @Override
    protected void performAction(ServerPlayerEntity player) {
        // TODO
        System.out.println("Performed command: " + command);
    }

}
