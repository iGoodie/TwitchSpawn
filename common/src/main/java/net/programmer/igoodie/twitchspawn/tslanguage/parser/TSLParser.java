package net.programmer.igoodie.twitchspawn.tslanguage.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.programmer.igoodie.twitchspawn.tslanguage.TSLFlowNode;
import net.programmer.igoodie.twitchspawn.tslanguage.action.TSLAction;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEvent;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLActionKeyword;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLComparatorSymbol;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLEventKeyword;
import net.programmer.igoodie.twitchspawn.tslanguage.predicate.TSLComparator;
import net.programmer.igoodie.twitchspawn.tslanguage.predicate.TSLPredicate;
import net.programmer.igoodie.twitchspawn.util.GsonUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TSLParser {

    private static Pattern PERCENTAGE_PATTERN = Pattern.compile("(?<decimal>\\d{1,3})(\\.(?<fraction>\\d{1,2}))?");

    public static int parsePercentage(String percentageString) {
        Matcher matcher = PERCENTAGE_PATTERN.matcher(percentageString);

        if (!matcher.matches())
            throw new IllegalArgumentException("Unexpected percentage format -> " + percentageString);

        try {
            String decimalGroup = matcher.group("decimal");
            String fractionGroup = matcher.group("fraction");

            int decimal = Integer.parseInt(decimalGroup);
            int fraction = fractionGroup == null ? 0 : Integer.parseInt(String.format("%-2s", fractionGroup).replace(' ', '0'));

            return decimal * 100 + fraction;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unexpected percentage format -> " + percentageString);
        }
    }

    public static JsonArray parseMessage(List<String> words) throws TSLSyntaxError {
        long messageKeywordCount = words.stream()
                .filter(word -> word.equalsIgnoreCase(TSLRuleTokenizer.DISPLAY_KEYWORD))
                .count();

        if (messageKeywordCount == 0)
            return null; // Nothing to parse

        if (messageKeywordCount != 1)
            throw new TSLSyntaxError("Expected AT MOST one %s, found %d instead",
                    TSLRuleTokenizer.DISPLAY_KEYWORD, messageKeywordCount);

        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);

            // Found the displaying keyword
            if (word.equalsIgnoreCase(TSLRuleTokenizer.DISPLAY_KEYWORD)) {

                // Range check
                if (i + 1 >= words.size())
                    throw new TSLSyntaxError("Expected word after %s", TSLRuleTokenizer.DISPLAY_KEYWORD);

                String jsonString = words.get(i + 1);

                // Yet another hack for backwards compatibility
                if (jsonString.equalsIgnoreCase("NOTHING")) {
                    JsonArray displayNothing = new JsonArray();
                    displayNothing.add("NOTHING_0xDEADC0DE_0xDEADBEEF"); // <-- Super hyper mega hacker move
                    return displayNothing;
                }

                try {
                    JsonArray parsedMessage = new JsonParser().parse(jsonString).getAsJsonArray();
                    GsonUtils.removeInvalidTextComponent(parsedMessage); // <-- Will also remove null elements created by trailing comma chars
                    return parsedMessage;

                } catch (JsonParseException e) {
                    throw new TSLSyntaxError("Malformed JSON array -> %s", jsonString);

                } catch (IllegalStateException e) {
                    throw new TSLSyntaxError("Expected JSON array, found instead -> %s", jsonString);
                }
            }
        }

        return null;
    }

    public static TSLAction parseAction(String actionName, List<String> actionParameters) throws TSLSyntaxError {
        Class<? extends TSLAction> actionClass = TSLActionKeyword.toClass(actionName);

        if (actionClass == null)
            throw new TSLSyntaxError("Unknown action name -> %s", actionName);

        try {
            return (TSLAction) actionClass.getConstructors()[0].newInstance(actionParameters);

        } catch (InstantiationException e) {
            throw new InternalError("Tried to instantiate an abstract class -> " + actionClass.getName());

        } catch (IllegalAccessException e) {
            throw new InternalError("Tried to instantiate an inaccessible class -> " + actionClass.getName());

        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof TSLSyntaxError)
                throw (TSLSyntaxError) e.getCause();
            e.getCause().printStackTrace();
            throw new InternalError("Constructor threw unexpected Throwable: " + e.getClass().getSimpleName()
                    , e.getCause());

        } catch (ClassCastException e) {
            throw new InternalError("Cannot cast " + actionClass.getSimpleName() + " to " + TSLAction.class.getSimpleName());
        }
    }

    public static TSLEvent parseEvent(String eventName) throws TSLSyntaxError {
        if (!TSLEventKeyword.exists(eventName))
            throw new TSLSyntaxError("Unknown event name -> %s", eventName);

        return new TSLEvent(eventName);
    }

    public static List<TSLPredicate> parsePredicates(List<List<String>> predicateParameters) throws TSLSyntaxError {
        List<TSLPredicate> predicates = new LinkedList<>();

        // For each word sequence
        for (List<String> predicateWords : predicateParameters) {
            if (predicateWords.size() < 3)
                throw new TSLSyntaxError("Expected at least 3 words after %s, found instead -> %s",
                        TSLRuleTokenizer.PREDICATE_KEYWORD, predicateWords);

            // Copy predicate words, will consume them
            List<String> remainingWords = new LinkedList<>(predicateWords);

            // Consume rightmost and leftmost words, join remaining
            String propertyName = remainingWords.remove(0);
            String rightHand = remainingWords.remove(remainingWords.size() - 1);
            String symbol = String.join(" ", remainingWords);

            // Create predicate and accumulate
            predicates.add(new TSLPredicate(propertyName, parseComparator(symbol, rightHand)));
        }

        return predicates;
    }

    public static TSLComparator parseComparator(String symbol, String rightHand) throws TSLSyntaxError {
        Class<? extends TSLComparator> comparatorClass = TSLComparatorSymbol.toClass(symbol);

        if (comparatorClass == null)
            throw new TSLSyntaxError("Unknown comparator -> %s", symbol);

        try {
            return (TSLComparator) comparatorClass.getConstructors()[0].newInstance(rightHand);

        } catch (InstantiationException e) {
            throw new InternalError("Tried to instantiate an abstract class -> " + comparatorClass.getName());

        } catch (IllegalAccessException e) {
            throw new InternalError("Tried to instantiate an inaccessible class -> " + comparatorClass.getName());

        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof TSLSyntaxError)
                throw (TSLSyntaxError) e.getCause();
            throw new InternalError("Constructor threw unexpected Throwable: " + e.getCause().getClass().getSimpleName());

        } catch (ClassCastException e) {
            throw new InternalError("Cannot cast " + comparatorClass.getSimpleName() + " to " + TSLComparator.class.getSimpleName());
        }
    }

    /* ------------------------------------ */

    private TSLTokenizer tokenizer;
    private Map<String, TSLEvent> events; // E.g "twitch follow" -> { TSLEvent }

    public TSLParser(String script) {
        this.tokenizer = new TSLTokenizer(script);
        this.events = new HashMap<>();
    }

    public Map<String, TSLEvent> parse() throws TSLSyntaxErrors {
        // tokenize into rules first
        try { tokenizer.intoRules(); } catch (TSLSyntaxError e) { throw new TSLSyntaxErrors(e); }

        List<TSLSyntaxError> syntaxErrors = new LinkedList<>();

        // Traverse every rule
        for (int i = 0; i < tokenizer.ruleCount(); i++) {
            try {
                List<String> words = tokenizer.intoWords(i);

                TSLRuleTokenizer ruleParts = new TSLRuleTokenizer(words).intoParts();

                // Fetch event, or create one
                TSLEvent event = events.containsKey(ruleParts.getEventName().toLowerCase())
                        ? events.get(ruleParts.getEventName().toLowerCase())
                        : parseEvent(ruleParts.getEventName());

                // Parse action and predicates
                TSLAction action = parseAction(ruleParts.getActionName(), ruleParts.getActionParameters());
                List<TSLPredicate> predicates = parsePredicates(ruleParts.getPredicateParameters());

                // Chain all the nodes on event node
                chainAll(event, predicates, action);

                // Put event to where it belongs
                events.put(event.getName().toLowerCase(), event);

            } catch (TSLSyntaxError e) {
                e.setAssociatedRule(tokenizer.getRule(i));
                syntaxErrors.add(e);
            }
        }

        if (!syntaxErrors.isEmpty())
            throw new TSLSyntaxErrors(syntaxErrors);

        return events;
    }

    private void chainAll(TSLEvent event, List<TSLPredicate> predicates, TSLAction action) {
        TSLFlowNode current = event;

        for (TSLPredicate predicate : predicates) {
            current = current.chain(predicate);
        }

        current.chain(action);
    }

}
