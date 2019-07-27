package net.programmer.igoodie.twitchspawn.tslanguage.action;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

import java.util.List;

public class NothingAction extends TSLAction {

    public NothingAction(List<String> words) throws TSLSyntaxError {
        if (words.size() != 0)
            throw new TSLSyntaxError("Expected 0 arguments, found -> " + words.size());
    }

    @Override
    protected void performAction(ServerPlayerEntity player) {}

    @Override
    protected String subtitleEvaluator(String expression, EventArguments args) {
        return null; // No extra evaluation
    }

}
