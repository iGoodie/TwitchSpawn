package net.programmer.igoodie.twitchspawn.tslanguage.parser;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class TSLTokenizer {

    public static final String MULTI_LINE_COMMENT_BEGIN = "#*";
    public static final String MULTI_LINE_COMMENT_END = "*#";
    public static final char COMMENT = '#';
    public static final char SPACE = ' ';
    public static final char GROUPING = '%';
    public static final char ESCAPE = '\\';

    public static List<String> intoRules(String script) throws TSLSyntaxError {
        TSLTokenizer tokenizer = new TSLTokenizer(script);
        return tokenizer.intoRules();
    }

    public static List<String> intoWords(String rule) throws TSLSyntaxError {
        TSLTokenizer tokenizer = new TSLTokenizer(rule);
        tokenizer.intoRules(); // tokenize into rules first
        return tokenizer.intoWords(0);
    }

    public static String buildRule(List<String> words) {
        StringBuilder builder = new StringBuilder();

        for (String word : words) {
            // Escape any QUOTE
            word = word.replace(GROUPING + "", ESCAPE + "" + GROUPING);

            // Group word, if it contains SPACE
            if (word.contains(SPACE + ""))
                word = GROUPING + word + GROUPING;

            // Append delimiter if not the first word
            if (builder.length() != 0) builder.append(SPACE);

            // Append word
            builder.append(word);
        }

        return builder.toString();
    }

    /* ------------------------------ */

    private String script;
    private List<String> rules;

    public TSLTokenizer(String script) {
        this.script = script;
    }

    public int ruleCount() {
        return rules.size();
    }

    public String getRule(int index) {
        // Assert given index range
        if (index < 0 || rules.size() <= index)
            throw new IndexOutOfBoundsException(String.format("Out of bounds -> %d [0,%d]", index, rules.size() - 1));

        return rules.get(index);
    }

    public List<String> intoRules() throws TSLSyntaxError {
        this.rules = new LinkedList<>();
        String[] lines = script
                .replaceAll("(?s)" + quoteRegex(MULTI_LINE_COMMENT_BEGIN) + ".*?" + quoteRegex(MULTI_LINE_COMMENT_END), "") // Discard multi-line comments
                .split("\\R"); // Split newlines

        // Check for unexpected beginning or ending of a multi-line comment
        for (String line : lines) {
            if (line.contains(MULTI_LINE_COMMENT_BEGIN))
                throw new TSLSyntaxError("Unclosed multiline comment -> %s", line);
            if (line.contains(MULTI_LINE_COMMENT_END))
                throw new TSLSyntaxError("Unexpected comment closing -> %s", line);
        }

        StringBuilder rule = new StringBuilder();

        // Traverse every line
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // Skip comment lines
            if (line.matches("^[ \t]*" + quoteRegex(COMMENT) + ".*$"))
                continue;

            // Trim valid comments
            line = trimComments(line);

            // Empty line (rule delimiter) occurred
            if (line.matches("\\s*")) {
                if (rule.length() != 0) rules.add(rule.toString());
                rule.setLength(0); // Reset rule accumulator
                continue;
            }

            // Starts with indent
            if (line.matches("^[ \t].*$")) {
                if (rule.length() == 0)
                    throw new TSLSyntaxError("Invalid indent at line " + (i + 1));

                rule.append(SPACE).append(line.trim());
                continue;
            }

            // No indent & not empty line (rule delimiter)
            if (rule.length() != 0)
                throw new TSLSyntaxError("Missing indent at line " + (i + 1));

            rule.setLength(0);
            rule.append(line.trim());
        }

        // Add last accumulation if not empty
        if (rule.length() != 0)
            rules.add(rule.toString());

        return rules;
    }

    public List<String> intoWords(int ruleIndex) throws TSLSyntaxError {
        if (rules == null)
            throw new IllegalStateException("Cannot use intoWords() before intoRules()");

        List<String> words = new LinkedList<>();

        // Assert given index range
        if (ruleIndex < 0 || rules.size() <= ruleIndex)
            throw new IndexOutOfBoundsException(String.format("Out of bounds -> %d [0,%d]", ruleIndex, rules.size() - 1));

        String rule = rules.get(ruleIndex);

        StringBuilder word = new StringBuilder();
        boolean inGroup = false;
        boolean escaping = false;

        // Traverse each character and accumulate words
        for (char character : rule.toCharArray()) {

            // Escape character
            if (character == ESCAPE) {
                if (escaping) {
                    word.append(ESCAPE);
                    escaping = false;
                } else escaping = true;
                continue;
            }

            // Grouping character
            if (character == GROUPING) {
                if (escaping) {
                    word.append(GROUPING);
                    escaping = false;
                    continue;
                }

                inGroup = !inGroup;
                continue;
            }

            // Space character
            if (character == SPACE) {
                if (escaping)
                    throw new TSLSyntaxError("Unexpected escaping on space character near -> " + word);

                if (!inGroup) {
                    if (word.length() != 0)
                        words.add(word.toString());
                    word.setLength(0);
                    continue;
                }
            }

            // None of them
            if (escaping) {
                escaping = false;
                word.append(ESCAPE);
            }
            word.append(character);
        }

        // Add last accumulation if it is not empty
        if (word.length() != 0)
            words.add(word.toString());

        if (escaping) // Still escaping
            throw new TSLSyntaxError("Incomplete escape");

        if (inGroup) // Still in group
            throw new TSLSyntaxError("Incomplete grouping sequence");

        return words;
    }

    private String quoteRegex(char character) {
        return "\\" + character;
    }

    private String quoteRegex(String string) {
        return Pattern.quote(string);
    }

    private String trimComments(String line) {
        StringBuilder trimmed = new StringBuilder();

        boolean escaping = false;
        boolean inGroup = false;
        boolean inJsonString = false;

        for (char character : line.toCharArray()) {

            // Comment character
            if (character == COMMENT) {
                if (!inGroup && !inJsonString)
                    break;
            }

            // Grouping character
            if (character == GROUPING) {
                if (!escaping)
                    inGroup = !inGroup;
            }

            // Json String
            if (character == '"' || character == '\'') {
                if (!escaping)
                    inJsonString = !inJsonString;
            }

            // Escape character
            if (character == ESCAPE) {
                escaping = !escaping;
                trimmed.append(character);
                continue;
            }

            trimmed.append(character);
            escaping = false;
        }

        return trimmed.toString();
    }

}
