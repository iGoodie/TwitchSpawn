package net.programmer.igoodie.twitchspawn.tslanguage.action;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class EitherAction extends TSLAction {

    List<TSLAction> actions;
    TSLAction selectedAction;

    public EitherAction(List<String> words) throws TSLSyntaxError {
        this.actions = new ArrayList<>();

        parseActions(words);

        if (this.actions.size() < 2)
            throw new TSLSyntaxError("Expected at least 2 actions, found -> " + this.actions.size());

        selectRandomAction();
    }

    private void parseActions(List<String> words) throws TSLSyntaxError {
        int lastOrIndex = -1;
        String actionAlias = "";
        List<String> actionArgs = new LinkedList<>();

        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);

            // Found an OR word
            if (word.equalsIgnoreCase("OR")) {
                if (actionAlias.isEmpty())
                    throw new TSLSyntaxError("Found OR word at an unexpected position.");
                addAction(actionAlias, actionArgs);
                lastOrIndex = i;
                actionAlias = "";
                actionArgs.clear();
                continue;
            }

            // Word next to OR
            if (lastOrIndex == (i - 1)) {
                actionAlias = word;
                continue;
            }

            actionArgs.add(word);
        }

        // Execute once for the last action script
        if (actionAlias.isEmpty())
            throw new TSLSyntaxError("Found OR word at an unexpected position.");
        addAction(actionAlias, actionArgs);
    }

    private void addAction(String actionAlias, List<String> actionArgs) throws TSLSyntaxError {
        Class<? extends TSLAction> actionClass = TSLParser.getActionClass(actionAlias);

        // No class found with that input
        if (actionClass == null)
            throw new TSLSyntaxError("Unexpected action -> " + actionAlias);

        // Parse action and save it here
        TSLAction action = TSLParser.createInstance(actionClass, actionArgs);
        this.actions.add(action);
    }

    private void selectRandomAction() {
        int randIndex = (int) (Math.random() * actions.size());
        selectedAction = actions.get(randIndex);
    }

    @Override
    protected void performAction(ServerPlayerEntity player) {
        selectedAction.performAction(player);
        selectRandomAction();
    }

    @Override
    protected String subtitleEvaluator(String expression, EventArguments args) {
        return selectedAction.subtitleEvaluator(expression, args);
    }

    @Override
    protected String associatedSubtitleAction() {
        return TSLParser.getActionName(selectedAction.getClass());
    }

}
