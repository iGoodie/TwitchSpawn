package net.programmer.igoodie.twitchspawn.tslanguage.action;

import net.minecraft.entity.player.EntityPlayerMP;
import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLActionKeyword;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

import java.util.List;

public class ForAction extends TSLAction {

    private TSLAction action;
    private int iterationCount;

    /*
     * FOR 5 TIMES
     *  SUMMON zombie
     *  ON ...
     *
     * Word#0 -> 5
     * Word#1 -> TIMES
     * Word#[2,n) -> <action>
     */
    public ForAction(List<String> words) throws TSLSyntaxError {
        this.message = TSLParser.parseMessage(words);
        List<String> actionWords = actionPart(words);

        if (actionWords.size() < 3)
            throw new TSLSyntaxError("Invalid length of words: " + actionWords);

        if (!actionWords.get(1).equalsIgnoreCase("TIMES"))
            throw new TSLSyntaxError("Expected TIMES, but found -> %s", actionWords.get(1));

        try {
            this.iterationCount = Integer.parseInt(actionWords.get(0));
            this.action = TSLParser.parseAction(words.get(2), words.subList(3, words.size()));
            this.action.silent = true;

        } catch (NumberFormatException e) {
            throw new TSLSyntaxError("Malformed number word -> %s", actionWords.get(0));
        }
    }

    @Override
    protected void performAction(EntityPlayerMP player, EventArguments args) {
        action.reflectedUser = this.reflectedUser;

        for (int i = 0; i < iterationCount; i++) {
            action.performAction(player, args);
        }
    }

    @Override
    protected String associatedSubtitleAction() {
        return TSLActionKeyword.ofClass(action.getClass());
    }

    @Override
    protected String subtitleEvaluator(String expression, EventArguments args) {
        if (expression.equals("loopCount"))
            return String.valueOf(iterationCount);
        return null;
    }

}
