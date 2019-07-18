package net.programmer.igoodie.twitchspawn.tslanguage.parser;

import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.tslanguage.TSLFlowNode;
import net.programmer.igoodie.twitchspawn.tslanguage.action.CommandBlockAction;
import net.programmer.igoodie.twitchspawn.tslanguage.action.DropAction;
import net.programmer.igoodie.twitchspawn.tslanguage.action.SummonAction;
import net.programmer.igoodie.twitchspawn.tslanguage.action.TSLAction;
import net.programmer.igoodie.twitchspawn.tslanguage.event.StreamlabsDonationEvent;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEvent;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TwitchFollowEvent;
import net.programmer.igoodie.twitchspawn.tslanguage.predicate.GreaterThanComparator;
import net.programmer.igoodie.twitchspawn.tslanguage.predicate.InRangeComparator;
import net.programmer.igoodie.twitchspawn.tslanguage.predicate.TSLComparator;
import net.programmer.igoodie.twitchspawn.tslanguage.predicate.TSLPredicate;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;

public class TSLParser {

    private static final char SPACE = ' ';
    private static final char QUOTE = '"';
    private static final char ESCAPE_CHAR = '\\';

    private static Map<String, Class<? extends TSLAction>> ACTION_CLASSES;
    private static Map<String, Class<? extends TSLComparator>> COMPARATOR_CLASSES;
    private static Map<String, Class<? extends TSLEvent>> EVENT_CLASSES;

    public static void initialize() {
        EVENT_CLASSES = new HashMap<>();
        ACTION_CLASSES = new HashMap<>();
        COMPARATOR_CLASSES = new HashMap<>();

        registerEvent(StreamlabsDonationEvent.class);
        registerEvent(TwitchFollowEvent.class);

        registerAction("DROP", DropAction.class);
        registerAction("SUMMON", SummonAction.class);
        registerAction("EXECUTE", CommandBlockAction.class);

        registerComparator(InRangeComparator.class);
        registerComparator(GreaterThanComparator.class);
    }

    private static void registerEvent(Class<? extends TSLEvent> eventClass) {
        try {
            Field descriptionField = eventClass.getField("DESCRIPTION");
            Field eventTypeField = eventClass.getField("EVENT_TYPE");
            Field eventForField = eventClass.getField("EVENT_FOR");

            if (!Modifier.isFinal(descriptionField.getModifiers()))
                throw new InternalError("TSLEvent's DESCRIPTION field must be constant");
            if (!Modifier.isFinal(eventTypeField.getModifiers()))
                throw new InternalError("");
            if (!Modifier.isFinal(eventForField.getModifiers()))
                throw new InternalError("");

            String description = (String) descriptionField.get(null);
            String eventType = (String) eventTypeField.get(null);
            String eventFor = (String) eventForField.get(null);

            EVENT_CLASSES.put(description, eventClass);
            TwitchSpawn.LOGGER.info("Registered TSLEvent key: {} -> {}",
                    description, eventClass.getSimpleName());

        } catch (NoSuchFieldException e) {
            throw new InternalError("TSLEvent must have public DESCRIPTION, EVENT_TYPE and EVENT_FOR fields -> "
                    + eventClass.getSimpleName());
        } catch (NullPointerException e) {
            throw new InternalError("TSLEvent's DESCRIPTION, EVENT_TYPE and EVENT_FOR fields must be static -> "
                    + eventClass.getSimpleName());
        } catch (IllegalAccessException e) {
            throw new InternalError("TSLEvent's DESCRIPTION, EVENT_TYPE and EVENT_FOR fields must be public -> "
                    + eventClass.getSimpleName());
        } catch (ClassCastException e) {
            throw new InternalError("TSLEvent's DESCRIPTION, EVENT_TYPE and EVENT_FOR fields must be a String -> "
                    + eventClass.getSimpleName());
        }
    }

    public static void registerAction(String name, Class<? extends TSLAction> actionClass) {
        ACTION_CLASSES.put(name, actionClass);
        TwitchSpawn.LOGGER.info("Registered TSLAction key: {} -> {}",
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
            TwitchSpawn.LOGGER.info("Registered TSLComparator key: {} -> {}",
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

        Class<? extends TSLEvent> eventClass;

        // Event name not found
        if ((eventClass = EVENT_CLASSES.get(eventName)) == null)
            throw new TSLSyntaxError("Unexpected event description -> " + eventName);

        // Return existing event or create one
        return events.getOrDefault(eventName, createInstance(eventClass));
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

        String parameterName = arguments.remove(0);
        String rightHandRaw = arguments.remove(arguments.size() - 1);
        String symbol = String.join(" ", arguments);

        return new TSLPredicate(parameterName, createComparator(symbol, rightHandRaw));
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

        // Put the event on/back to the event cache
        String eventDescription = getClassProperty(event.getClass(), "DESCRIPTION");
        events.put(eventDescription, event);
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

    public static void main(String[] args) {
//        try {
//            TSLParser.initialize();
//            File file = new File("C:/twitchspawntest.tsl");
//            String script = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
//
//            TSLParser parser = new TSLParser(script);
//
//            parser.parse();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (TSLSyntaxError e) {
//            e.printStackTrace();
//        } catch (TSLSyntaxErrors e) {
//            e.getErrors().forEach(Exception::printStackTrace);
//        }

        try {
            TSLParser.initialize();
            File file = new File("C:/twitchspawntest.tsl");
            String input = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            TSLParser parser = new TSLParser(input);

            System.out.println("# --------------RAW-INPUT---------- #");
            System.out.println(input);
            System.out.println("# --------------RULES-------------- #");

            parser.parseRules(input).forEach(rule -> {
                try {
                    System.out.println(rule);
//                    parseWords(rule).forEach(System.out::println);
                    parser.parse(rule);
                } catch (TSLSyntaxError e) {
                    e.printStackTrace();
                } catch (InternalError e) {
                    e.printStackTrace();
                }
                System.out.println("# --------------------------------- #");
            });

        } catch (IOException e) {
            e.printStackTrace();

        } catch (TSLSyntaxError e) {
            e.printStackTrace();
        }

//        String symbol = "IN";
//        String indentRegex = "[ \t]+";
//
//        String input = "IN   [0 ,   20, 30, 40]";
//        String regex = Pattern.quote(symbol) + indentRegex
//                + "\\[(\\d+)(?:[ \t]*,[ \t]*(\\d+))*\\]";
//
//        Pattern pattern = Pattern.compile(regex);
//        Matcher matcher = pattern.matcher(input);
//
//        if (matcher.find()) {
//            System.out.println(matcher.group());
//            for (int i = 0; i <= matcher.groupCount(); i++)
//                System.out.printf("GRP#%d - %s\n", i, matcher.group(i));
//        }
//
//        Class<? extends TSLComparator> comp = InRangeComparator.class;
//
//        try {
//            System.out.println(TSLComparator.SYMBOL);
//            System.out.println(comp.getField("SYMBOL").get(null));
//
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//            e.printStackTrace();
//            System.err.println("TSLComparator extensions should have a public SYMBOL variable!");
//        }
//
//        Object a = "Foo";
//
//        System.out.println((int) a);
    }
}
