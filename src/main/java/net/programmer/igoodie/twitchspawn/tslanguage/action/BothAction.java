package net.programmer.igoodie.twitchspawn.tslanguage.action;

import com.google.gson.JsonArray;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLActionKeyword;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;
import net.programmer.igoodie.twitchspawn.eventqueue.EventQueue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BothAction extends TSLAction {

    public static final String DELIMITER = "AND";

    private boolean instant;
    private List<TSLAction> actions;

    public BothAction(List<String> words) throws TSLSyntaxError {
        this.instant = words.get(0).equalsIgnoreCase("INSTANTLY");
        this.actions = new ArrayList<>();

        List<String> actionWords = actionPart(words);

        if (instant) {
            this.message = TSLParser.parseMessage(words); // Expecting only 1 DISPLAYING
            parseActions(actionWords.subList(1, actionWords.size()));
            this.actions.forEach(action -> action.silent = true);

        } else {
            this.silent = true; // No notification for BOTH INSTANTLY action
            if (words.get(words.size() - 3).equals("ALL")) { // ... ALL DISPLAYING %[...]% ...
                JsonArray message = TSLParser.parseMessage(words); // Expecting only 1 DISPLAYING
                actionWords = actionPart(words, "ALL"); // Re-split actions until ALL keyword
                parseActions(actionWords);
                this.actions.forEach(action -> action.message = message); // Put the message to each event

            } else {
                parseActions(words);
            }
        }

        if (this.actions.size() < 2)
            throw new TSLSyntaxError("Expected at least 2 actions, found -> " + this.actions.size());
    }

    private void parseActions(List<String> words) throws TSLSyntaxError {
        int lastDelimiterIndex = -1;
        String actionAlias = "";
        List<String> actionArgs = new LinkedList<>();

        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);

            // Found a delimiter
            if (word.equalsIgnoreCase(DELIMITER)) {
                if (actionAlias.isEmpty())
                    throw new TSLSyntaxError(String.format("Found %s word at an unexpected position. (Word#%d)", DELIMITER, i));
                parseSingleAction(actionAlias, actionArgs);
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
            throw new TSLSyntaxError("Found %s word at an unexpected position. (Word#%d)", DELIMITER, words.size() - 1);
        parseSingleAction(actionAlias, actionArgs);
    }

    private void parseSingleAction(String actionName, List<String> actionArgs) throws TSLSyntaxError {
        if (TSLActionKeyword.toClass(actionName) == this.getClass())
            throw new TSLSyntaxError("Cannot chain %s action with another %s action",
                    TSLActionKeyword.ofClass(this.getClass()), TSLActionKeyword.ofClass(this.getClass()));

        TSLAction action = TSLParser.parseAction(actionName, actionArgs);
        this.actions.add(action);
    }

    @Override
    protected void performAction(ServerPlayerEntity player, EventArguments args) {
        this.actions.forEach(action -> action.reflectedUser = this.reflectedUser);

        EventQueue eventQueue = ConfigManager.RULESET_COLLECTION.getQueue(args.streamerNickname);

        if (instant) { // Perform them all instantly
            for (int i = this.actions.size() - 1; i >= 0; i--) {
                TSLAction action = this.actions.get(i);
                eventQueue.queueFirst(action.getClass().getSimpleName(),
                        () -> action.performAction(player, args));
            }

        } else {
            for (int i = this.actions.size() - 1; i >= 0; i--) {
                TSLAction action = this.actions.get(i);
                eventQueue.queueFirst(action.getClass().getSimpleName(),
                        () -> action.process(args));
                if (i != 0) eventQueue.queueSleepFirst(ConfigManager.PREFERENCES.notificationDelay);
            }
        }

        eventQueue.updateThread();
    }

}
