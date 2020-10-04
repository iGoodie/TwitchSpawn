package net.programmer.igoodie.twitchspawn.tslanguage.action;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLActionKeyword;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;
import net.programmer.igoodie.twitchspawn.util.ExpressionEvaluator;

import java.util.List;

public class ForAction extends TSLAction {

    private TSLAction action;
    private String iterationCount;

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
            this.iterationCount = actionWords.get(0);
            this.action = TSLParser.parseAction(words.get(2), words.subList(3, words.size()));
            this.action.silent = true;

            // Check if given iteration count is parse-able
            EventArguments randomEvent = EventArguments.createRandom("RandomStreamer");
            evaluateIterationCount(randomEvent);

        } catch (NumberFormatException e) {
            throw new TSLSyntaxError("Malformed number word -> %s", actionWords.get(0));
        }
    }

    @Override
    protected void performAction(ServerPlayerEntity player, EventArguments args) {
        action.reflectedUser = this.reflectedUser;

        int iterationCount = evaluateIterationCount(args);

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
            return String.valueOf(evaluateIterationCount(args));
        return action.subtitleEvaluator(expression, args);
    }

    private int evaluateIterationCount(EventArguments args) {
        String iterationCountEvaluated = ExpressionEvaluator.replaceExpressions(this.iterationCount, expression -> {
            return ExpressionEvaluator.fromArgs(expression, args);
        });

        return Double.valueOf(iterationCountEvaluated).intValue();
    }

}
