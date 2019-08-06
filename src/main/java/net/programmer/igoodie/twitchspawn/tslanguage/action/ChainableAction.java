package net.programmer.igoodie.twitchspawn.tslanguage.action;

import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class ChainableAction extends TSLAction {

    protected List<TSLAction> actions;
    protected String delimiter;

    protected ChainableAction(String delimiter) throws TSLSyntaxError {
        this.actions = new ArrayList<>();
        this.delimiter = delimiter;
    }

    protected void parseActions(List<String> words) throws TSLSyntaxError {
        int lastDelimiterIndex = -1;
        String actionAlias = "";
        List<String> actionArgs = new LinkedList<>();

        System.out.println(words);

        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);

            // Found a delimiter
            if (word.equalsIgnoreCase(delimiter)) {
                System.out.println(actionAlias + " " + actionArgs);
                if (actionAlias.isEmpty())
                    throw new TSLSyntaxError(String.format("Found %s word at an unexpected position. (Word#%d)", delimiter, i));
                addAction(actionAlias, actionArgs);
                lastDelimiterIndex = i;
                actionAlias = "";
                actionArgs.clear();
                continue;
            }

            // Word next to the delimiter
            if (lastDelimiterIndex == (i - 1)) {
                actionAlias = word;
                continue;
            }

            actionArgs.add(word);
        }

        // Execute once for the last action script
        if (actionAlias.isEmpty())
            throw new TSLSyntaxError(String.format("Found %s word at an unexpected position. (Word#%d)",
                    delimiter, words.size() - 1));
        addAction(actionAlias, actionArgs);
    }

    protected void addAction(String actionAlias, List<String> actionArgs) throws TSLSyntaxError {
        Class<? extends TSLAction> actionClass = TSLParser.getActionClass(actionAlias);

        // No class found with that input
        if (actionClass == null)
            throw new TSLSyntaxError("Unexpected action -> " + actionAlias);

        // Parse action and save it here
        TSLAction action = TSLParser.createInstance(actionClass, actionArgs);
        this.actions.add(action);
    }

}
