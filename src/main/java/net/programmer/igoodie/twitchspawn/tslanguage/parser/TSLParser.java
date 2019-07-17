package net.programmer.igoodie.twitchspawn.tslanguage.parser;

import net.programmer.igoodie.twitchspawn.tslanguage.action.CommandBlockAction;
import net.programmer.igoodie.twitchspawn.tslanguage.action.DropAction;
import net.programmer.igoodie.twitchspawn.tslanguage.action.SummonAction;
import net.programmer.igoodie.twitchspawn.tslanguage.action.TSLAction;
import net.programmer.igoodie.twitchspawn.tslanguage.event.StreamlabsDonationEvent;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TSLEvent;
import net.programmer.igoodie.twitchspawn.tslanguage.event.TwitchFollowEvent;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TSLParser {

    private static final char SPACE = ' ';
    private static final char QUOTE = '"';
    private static final char ESCAPE_CHAR = '\\';

    private static Map<String, TSLEvent> EVENTS;
    private static Map<String, Class<? extends TSLAction>> ACTION_CLASSES;

    public static void initialize() {
        EVENTS = new HashMap<>();
        ACTION_CLASSES = new HashMap<>();

        ACTION_CLASSES.put("DROP", DropAction.class);
        ACTION_CLASSES.put("SUMMON", SummonAction.class);
        ACTION_CLASSES.put("EXECUTE", CommandBlockAction.class);

        EVENTS.put("streamlabs donation", new StreamlabsDonationEvent());
        EVENTS.put("twitch follow", new TwitchFollowEvent());
    }

    public static List<String> parseRules(String input) throws TSLSyntaxError {
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

    public static List<String> parseWords(String rule) throws TSLSyntaxError {
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

    public static TSLAction parseAction(List<String> words) throws TSLSyntaxError {
        int index = 0;
        String word = words.get(index++);

        String actionName = word;
        List<String> actionArguments = new LinkedList<>();
        Class<? extends TSLAction> actionClass;

        // Lookup for the action
        if ((actionClass = ACTION_CLASSES.get(actionName)) == null)
            throw new TSLSyntaxError("Unexpected action name " + actionName);

        // <ACTION> foo bar baz ON ...
        while (!(word = words.get(index++)).equalsIgnoreCase("ON")) {
            actionArguments.add(word);
        }

        return createActionInstance(actionClass, actionArguments);
    }

    private static <T> T createActionInstance(Class<T> clazz, Object... args) throws TSLSyntaxError {
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

    public static TSLEvent parseEvent(List<String> words) throws TSLSyntaxError {
        // Count ON statements
        if (words.stream().filter(word -> word.equalsIgnoreCase("ON")).count() != 1)
            throw new TSLSyntaxError("Expected exactly one ON statement");

        int index = 0;
        String word = words.get(index++);
        String eventName = "";

        TSLEvent event;

        // Skip all the part till ON statement
        while (!(word = words.get(index++)).equalsIgnoreCase("ON")) ;

        // ... ON Name Of The Event WHEN ...
        while ((index < words.size()) && !(word = words.get(index++)).equalsIgnoreCase("WITH")) {
            eventName += word + SPACE;
        }
        eventName = eventName.trim().toLowerCase();

        // No event name included
        if (eventName.isEmpty())
            throw new TSLSyntaxError("Expected an event name after ON keyword.");

        // Lookup for the event
        if ((event = EVENTS.get(eventName)) == null)
            throw new TSLSyntaxError("Unexpected event description: " + eventName);

        return event;
    }

    public static void parse(String rule) throws TSLSyntaxError {
        List<String> words = parseWords(rule);

        TSLEvent event = parseEvent(words);
        TSLAction action = parseAction(words);

        // TODO: Parse WITH statements
        // TODO: Create predicate instances with them

        event.append(action);
    }

    public static void main(String[] args) {
        try {
            TSLParser.initialize();
            File file = new File("C:/twitchspawntest.tsl");
            String input = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

            System.out.println("# --------------RAW-INPUT---------- #");
            System.out.println(input);
            System.out.println("# --------------RULES-------------- #");

            parseRules(input).forEach(rule -> {
                try {
                    System.out.println(rule);
//                    parseWords(rule).forEach(System.out::println);
                    parse(rule);
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
    }

}
