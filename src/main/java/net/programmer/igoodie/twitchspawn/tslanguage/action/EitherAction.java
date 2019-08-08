package net.programmer.igoodie.twitchspawn.tslanguage.action;

import com.google.gson.JsonArray;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLActionKeyword;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

import java.util.List;

public class EitherAction extends ChainableAction {

    private TSLAction selectedAction;

    public EitherAction(List<String> words) throws TSLSyntaxError {
        super("OR");

        if (words.get(words.size() - 3).equals("ALL")) { // ... ALL DISPLAYING %[...]% ...
            JsonArray message = TSLParser.parseMessage(words); // Expecting only 1 DISPLAYING
            List<String> actionWords = actionPart(words, "ALL");
            parseActions(actionWords);
            this.actions.forEach(action -> action.message = message); // Put the message to each event

        } else {
            parseActions(words);
        }


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
        if (!silent) selectedAction.process(args); // process() to include notification
        else selectedAction.performAction(player, args); // No need to include notification
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
