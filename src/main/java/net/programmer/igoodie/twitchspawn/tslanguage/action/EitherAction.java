package net.programmer.igoodie.twitchspawn.tslanguage.action;

import com.google.gson.JsonArray;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLActionKeyword;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;
import net.programmer.igoodie.twitchspawn.util.PercentageRandomizer;

import java.util.LinkedList;
import java.util.List;

public class EitherAction extends TSLAction {

    public static final String DELIMITER = "OR";

    private PercentageRandomizer<TSLAction> actions;
    private TSLAction selectedAction;

    public EitherAction(List<String> words) throws TSLSyntaxError {
        this.actions = new PercentageRandomizer<>();

        if (words.get(words.size() - 3).equals("ALL")) { // ... ALL DISPLAYING %[...]% ...
            JsonArray message = TSLParser.parseMessage(words); // Expecting only 1 DISPLAYING
            List<String> actionWords = actionPart(words, "ALL");
            parseActions(actionWords);
            this.actions.forEachElement(action -> action.message = message); // Put the message to each event

        } else {
            parseActions(words);
        }

        if (this.actions.size() < 2)
            throw new TSLSyntaxError("Expected at least 2 actions, found -> " + this.actions.size());

        selectedAction = actions.randomItem();
    }

    private void parseActions(List<String> words) throws TSLSyntaxError {
        List<List<String>> actionsRaw = splitActions(words);

        boolean chanceMode = actionsRaw.stream().anyMatch(this::containsPercentage);

        for (int i = 0; i < actionsRaw.size(); i++) {
            List<String> actionRaw = actionsRaw.get(i);

            if (chanceMode) {
                if (!containsPercentage(actionRaw))
                    throw new TSLSyntaxError("Expected chance information on rule#%d", (i + 1));

                try {
                    TSLAction action = parseSingleAction(actionRaw.subList(3, actionRaw.size()));
                    String percentage = actionRaw.get(1);
                    actions.addElement(action, percentage);

                } catch (IllegalStateException e) {
                    throw new TSLSyntaxError("Expected total of a 100.00%% probability, found -> %.02f%%",
                            actions.getTotalPercentage() / 100f);
                }

            } else {
                TSLAction action = parseSingleAction(actionRaw);
                actions.addElement(action, (100f / actionsRaw.size()));
            }
        }

        if (chanceMode && actions.getTotalPercentage() != 100_00)
            throw new TSLSyntaxError("Expected total of a 100.00%% probability, found -> %.02f%%",
                    actions.getTotalPercentage() / 100f);
    }

    private TSLAction parseSingleAction(List<String> actionWords) throws TSLSyntaxError {
        String actionName = actionWords.get(0);
        List<String> actionArgs = actionWords.subList(1, actionWords.size());

        if (TSLActionKeyword.toClass(actionName) == this.getClass())
            throw new TSLSyntaxError("Cannot chain %s action with another %s action",
                    TSLActionKeyword.ofClass(this.getClass()), TSLActionKeyword.ofClass(this.getClass()));

        return TSLParser.parseAction(actionName, actionArgs);
    }

    private List<List<String>> splitActions(List<String> words) {
        List<List<String>> actionsRaw = new LinkedList<>();
        List<String> actionRaw = new LinkedList<>();

        for (String word : words) {
            if (word.equalsIgnoreCase(DELIMITER)) {
                actionsRaw.add(actionRaw);
                actionRaw = new LinkedList<>();
                continue;
            }

            actionRaw.add(word);
        }

        actionsRaw.add(actionRaw);

        return actionsRaw;
    }

    private boolean containsPercentage(List<String> words) {
        if (words.size() < 3) return false;

        try {
            Double.parseDouble(words.get(1));
        } catch (NumberFormatException e) {
            return false;
        }

        return words.get(0).equalsIgnoreCase("CHANCE")
                && words.get(2).equalsIgnoreCase("PERCENT");
    }

    @Override
    protected void performAction(ServerPlayerEntity player, EventArguments args) {
        selectedAction.reflectedUser = this.reflectedUser;

        if (!silent) selectedAction.process(args); // process() to include notification
        else selectedAction.performAction(player, args); // No need to include notification
        selectedAction = actions.randomItem();
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
