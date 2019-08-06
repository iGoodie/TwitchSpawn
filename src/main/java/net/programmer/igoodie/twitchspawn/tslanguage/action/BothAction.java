package net.programmer.igoodie.twitchspawn.tslanguage.action;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

import java.util.List;

public class BothAction extends ChainableAction {

    private boolean instant;

    public BothAction(List<String> words) throws TSLSyntaxError {
        super("AND");

        this.instant = words.get(0).equalsIgnoreCase("INSTANT");

        parseActions(instant ? words.subList(1, words.size()) : words);

        this.silent = true; // No notification for BOTH action

        if (this.actions.size() < 2)
            throw new TSLSyntaxError("Expected at least 2 actions, found -> " + this.actions.size());
    }

    @Override
    protected void performAction(ServerPlayerEntity player, EventArguments args) {
        if (instant)
            ConfigManager.RULESET_COLLECTION.queue(() -> this.actions.forEach(action -> action.process(args)));
        else
            this.actions.forEach(action -> ConfigManager.RULESET_COLLECTION.queue(() -> action.process(args)));
    }

}
