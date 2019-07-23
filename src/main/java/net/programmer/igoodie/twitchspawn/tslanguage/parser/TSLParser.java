package net.programmer.igoodie.twitchspawn.tslanguage.parser;

import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.tslanguage.TSLFlowNode;
import net.programmer.igoodie.twitchspawn.tslanguage.action.CommandBlockAction;
import net.programmer.igoodie.twitchspawn.tslanguage.action.DropAction;
import net.programmer.igoodie.twitchspawn.tslanguage.action.SummonAction;
import net.programmer.igoodie.twitchspawn.tslanguage.action.TSLAction;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEvent;
import net.programmer.igoodie.twitchspawn.tslanguage.predicate.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.IntStream;

public class TSLParser {

    private static final char SPACE = ' ';
    private static final char QUOTE = '"';
    private static final char ESCAPE_CHAR = '\\';

    private static Map<String, Class<? extends TSLAction>> ACTION_CLASSES;
    private static Map<String, Class<? extends TSLComparator>> COMPARATOR_CLASSES;

    public static void initialize() {
        TwitchSpawn.LOGGER.info("Initializing TSL parsing specs...");

        ACTION_CLASSES = new HashMap<>();
        COMPARATOR_CLASSES = new HashMap<>();

        TSLEvent.loadEventAliases();
        TSLPredicate.loadPropertyAliases();

        registerAction("DROP", DropAction.class);
        registerAction("SUMMON", SummonAction.class);
        registerAction("EXECUTE", CommandBlockAction.class);

        registerComparator(InRangeComparator.class);
        registerComparator(GreaterThanComparator.class);
        registerComparator(GreaterThanOrEqComparator.class);
        registerComparator(LessThanComparator.class);
        registerComparator(LessThanOrEqComparator.class);

        TwitchSpawn.LOGGER.info("Initialized TSL parsing specs successfully");
    }

    public static void registerAction(String name, Class<? extends TSLAction> actionClass) {
        ACTION_CLASSES.put(name, actionClass);
        TwitchSpawn.LOGGER.debug("Registered TSLAction key: {} -> {}",
                name, actionClass.getSimpleName());
    }

    public static void registerComparator(Class<? extends TSLComparator> comparatorClass) {
        try {
            Field symbolField = comparatorClass.getField("SYMBOL");

            if (!Modifier.isFinal(symbolField.getModifiers()))
                throw new InternalError("TSLComparator's SYMBOL field must be constant (final). -> "
                        + comparatorClass.getSimpleName());

            String symbol = (String) symbolField.get(null);

            COMPARATOR_CLASSES.put(symbol, comparatorClass);
            TwitchSpawn.LOGGER.debug("Registered TSLComparator key: {} -> {}",
                    symbol, comparatorClass.getSimpleName());

        } catch (NoSuchFieldException e) {
            throw new InternalError("TSLComparator must have a public SYMBOL field -> "
                    + comparatorClass.getSimpleName());
        } catch (NullPointerException e) {
            throw new InternalError("TSLComparator's SYMBOL field must be static -> "
                    + comparatorClass.getSimpleName());
        } catch (IllegalAccessException e) {
            throw new InternalError("TSLComparator's SYMBOL field must be public -> "
                    + comparatorClass.getSimpleName());
        } catch (ClassCastException e) {
            throw new InternalError("TSLComparator's SYMBOL field must be a String -> "
                    + comparatorClass.getSimpleName());
        }
    }

    /* ------------------------------------------------------------------ */

    private static <T> T createInstance(Class<T> clazz, Object... args) throws TSLSyntaxError {
        try {
            return (T) clazz.getConstructors()[0].newInstance(args);

        } catch (InstantiationException e) {
            throw new InternalError("Tried to instantiate an abstract class: " + clazz.getName());
        } catch (IllegalAccessException e) {
            throw new InternalError("Tried to instantiate an unaccessable class:" + clazz.getName());
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof TSLSyntaxError)
                throw (TSLSyntaxError) e.getCause();
            throw new InternalError("Constructor threw unexpected Throwable: " + e.getCause().getClass().getSimpleName());
        }
    }

    private static int unescapedQuoteCount(String str) {
        int count = 0;

        for (int i = 1; i < str.length(); i++) {
            char character = str.charAt(i);
            char prevCharacter = str.charAt(i - 1);

            if (character != QUOTE)
                continue;

            if (prevCharacter != ESCAPE_CHAR)
                count++;
        }

        return count;
    }

    private static String getClassProperty(Class<?> clazz, String property) {
        try {
            Field field = clazz.getField(property);
            return (String) field.get(null);

        } catch (NoSuchFieldException e) {
            throw new InternalError("Property in class not found -> "
                    + clazz.getSimpleName() + ":" + property);
        } catch (NullPointerException e) {
            throw new InternalError("Property is non-static -> "
                    + clazz.getSimpleName() + ":" + property);
        } catch (IllegalAccessException e) {
            throw new InternalError("Property is non-accessible -> "
                    + clazz.getSimpleName() + ":" + property);
        } catch (ClassCastException e) {
            throw new InternalError("Property is not a String -> "
                    + clazz.getSimpleName() + ":" + property);
        }
    }

    /* ------------------------------------------------------------------ */

    private int wordIndex;
    private List<String> rules;
    private Map<String, TSLEvent> events;

    public TSLParser(String script) throws TSLSyntaxError {
        this.rules = parseRules(script);
        this.events = new HashMap<>();
        this.wordIndex = 0;
    }

    public List<String> parseRules(String input) throws TSLSyntaxError {
        List<String> rules = new LinkedList<>();
        String[] lines = input.split("\\R");

        String rule = "";

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // Skip comments
            if (line.matches("^[ \t]*#.*$"))
                continue;

            // Trim end of line comments
            // TODO: Keep them if they are in a word (E.g "Number#1")
            line = line.replaceAll("#.*", "");

            // Empty line
            if (line.matches("\\s*")) {
                if (rule.length() != 0) {
                    rules.add(rule);
                }
                rule = "";
                continue;
            }

            // Starts with indent
            if (line.matches("^[ \t].*$")) {
                if (rule.isEmpty())
                    throw new TSLSyntaxError("Invalid indent at line " + (i + 1));
                rule += SPACE + line.trim();
                continue;
            }

            // No indent, also not an empty line
            if (!rule.isEmpty())
                throw new TSLSyntaxError("Missing indent at line " + (i + 1));

            rule = line.trim();
        }

        if (rule.length() != 0) {
            rules.add(rule);
        }

        return rules;
    }

    public List<String> parseWords(String rule) throws TSLSyntaxError {
        if (unescapedQuoteCount(rule) % 2 != 0)
            throw new TSLSyntaxError("Invalid count of quotation marks");

        List<String> words = new LinkedList<>();

        String word = "";
        boolean inQuote = false;
        boolean escaping = false;

        for (int i = 0; i < rule.length(); i++) {
            char character = rule.charAt(i);

            if (escaping) {
                if (character != ESCAPE_CHAR && character != QUOTE)
                    throw new TSLSyntaxError("Invalid escape sequence at index " + i);

                word += character;
                escaping = false;
                continue;
            }

            if (character == ESCAPE_CHAR) {
                escaping = true;
                continue;
            }

            if (character == SPACE && !inQuote) {
                if (word.length() != 0) {
                    words.add(word);
                    word = "";
                }
                continue;
            }

            if (character == QUOTE) {
                if (inQuote) {
                    if (word.length() != 0) {
                        words.add(word);
                        word = "";
                    }
                }

                inQuote = !inQuote;
                continue;
            }

            word += character;
        }

        if (word.length() != 0)
            words.add(word);

        if (escaping)
            throw new TSLSyntaxError("Incompleted escape.");

        return words;
    }

    public TSLAction parseAction(List<String> words) throws TSLSyntaxError {
        String word = words.get(wordIndex++);

        String actionName = word;
        List<String> actionArguments = new LinkedList<>();
        Class<? extends TSLAction> actionClass;

        // Lookup for the action
        if ((actionClass = ACTION_CLASSES.get(actionName)) == null)
            throw new TSLSyntaxError("Unexpected action name -> " + actionName);

        // <ACTION> foo bar baz ON ...
        while (!(word = words.get(wordIndex)).equalsIgnoreCase("ON")) {
            actionArguments.add(word);
            wordIndex++;
        }

        return createInstance(actionClass, actionArguments);
    }

    public TSLEvent parseEvent(List<String> words) throws TSLSyntaxError {
        if (!words.get(wordIndex++).equalsIgnoreCase("ON"))
            throw new InternalError("Called TSLParser::parseEvent in an illegal state");

        String word = "";
        String eventName = "";

        // ... ON Name Of the Event WHEN ...
        // ... ON Name of the Event
        while ((wordIndex < words.size()) && !(word = words.get(wordIndex)).equalsIgnoreCase("WITH")) {
            eventName += word + SPACE;
            wordIndex++;
        }
        eventName = eventName.trim().toLowerCase();

        // No event name included
        if (eventName.isEmpty())
            throw new TSLSyntaxError("Expected an event name after ON keyword.");

        // Event name not found
        if (!TSLEvent.EVENT_NAME_ALIASES.containsValue(eventName))
            throw new TSLSyntaxError("Unexpected event description -> " + eventName);

        // Fetch or create event if necessary
        TSLEvent event = events.get(eventName);
        if (event == null)
            events.put(eventName, (event = new TSLEvent()));

        // Return the parsed TSLEvent
        return event;
    }

    public List<TSLPredicate> parsePredicates(List<String> words) throws TSLSyntaxError {
        if (wordIndex < words.size() && !words.get(wordIndex++).equalsIgnoreCase("WITH"))
            throw new InternalError("Called TSLParser::parsePredicates in an illegal state");

        List<TSLPredicate> predicates = new LinkedList<>();
        List<String> tokens = new LinkedList<>();
        String word;

        while (wordIndex < words.size()) {
            word = words.get(wordIndex);

            // Anything else than WITH
            if (!word.equalsIgnoreCase("WITH")) {
                tokens.add(word);
                wordIndex++;
                continue;
            }

            // WITH token!
            predicates.add(createPredicate(tokens));
            tokens.clear();
            wordIndex++;
        }

        // Create one last time before end of rule
        if (tokens.size() != 0)
            predicates.add(createPredicate(tokens));

        return predicates;
    }

    public TSLPredicate createPredicate(List<String> arguments) throws TSLSyntaxError {
        if (arguments.size() < 3)
            throw new TSLSyntaxError("Expected at least 3 words after WITH.");

        String fieldAlias = arguments.remove(0);
        String rightHandRaw = arguments.remove(arguments.size() - 1);
        String symbol = String.join(" ", arguments);

        return new TSLPredicate(fieldAlias, createComparator(symbol, rightHandRaw));
    }

    public TSLComparator createComparator(String symbol, String rightHandRaw) throws TSLSyntaxError {
        Class<? extends TSLComparator> comparatorClass;

        // No comparator with given symbol
        if ((comparatorClass = COMPARATOR_CLASSES.get(symbol)) == null)
            throw new TSLSyntaxError("Unexpected comparating operator -> " + symbol);

        return createInstance(comparatorClass, rightHandRaw);
    }

    public Map<String, TSLEvent> parse(String rule) throws TSLSyntaxError {
        this.wordIndex = 0; // Reset index cursor on fresh parse
        List<String> words = parseWords(rule);

        validate(words);

        TSLAction action = parseAction(words);
        TSLEvent event = parseEvent(words);
        List<TSLPredicate> predicates = parsePredicates(words);

        // Chain them all
        TSLFlowNode current = event;
        for (int i = 0; i < predicates.size(); i++) {
            current = current.chain(predicates.get(i));
        }
        current = current.chain(action);

        return this.events;
    }

    public Map<String, TSLEvent> parse() throws TSLSyntaxErrors {
        Iterator<String> ruleIterator = rules.iterator();
        List<TSLSyntaxError> errors = new LinkedList<>();

        while (ruleIterator.hasNext()) {
            try {
                parse(ruleIterator.next());

            } catch (TSLSyntaxError e) {
                errors.add(e);
            }
        }

        if (errors.size() > 0)
            throw new TSLSyntaxErrors(errors);

        return this.events;
    }

    private void validate(List<String> words) throws TSLSyntaxError {
        // Count ON statements
        if (words.stream().filter(word -> word.equalsIgnoreCase("ON")).count() != 1)
            throw new TSLSyntaxError("Expected exactly one ON statement");

        // First word is a reserved one
        if (words.get(0).matches("(?i:ON|WITH)"))
            throw new TSLSyntaxError("Unexpected statement on word #1 -> " + words.get(0));

        // Found WITH keyword before ON keyword
        int firstIndexWITH = IntStream.range(0, words.size())
                .filter(i -> words.get(i).equalsIgnoreCase("WITH"))
                .findFirst().orElse(-1);
        if (firstIndexWITH != -1) {
            int firstIndexON = IntStream.range(0, words.size())
                    .filter(i -> words.get(i).equalsIgnoreCase("ON"))
                    .findFirst().getAsInt();
            if (firstIndexWITH <= firstIndexON)
                throw new TSLSyntaxError("Unexpected location for WITH keyword at word #" + (firstIndexWITH + 1));
        }
    }

}
