package net.programmer.igoodie.twitchspawn.tslanguage.action;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLActionKeyword;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

import java.util.List;

public class EitherAction extends ChainableAction {

    private TSLAction selectedAction;

    public EitherAction(List<String> words) throws TSLSyntaxError {
        super("OR");

        parseActions(words);

        if (this.actions.size() < 2)
            throw new TSLSyntaxError("Expected at least 2 actions, found -> " + this.actions.size());

        selectRandomAction(); // Select next random action
    }

    private void selectRandomAction() {
        int randIndex = (int) (Math.random() * actions.size());
        selectedAction = actions.get(randIndex);
    }

    @Override
    protected void performAction(ServerPlayerEntity player, EventArguments args) {
        selectedAction.performAction(player, args);
        selectRandomAction();
    }

    @Override
    protected String subtitleEvaluator(String expression, EventArguments args) {
        return selectedAction.subtitleEvaluator(expression, args);
    }

    @Override
    protected String associatedSubtitleAction() {
        return TSLActionKeyword.ofClass(selectedAction.getClass());
    }

}
