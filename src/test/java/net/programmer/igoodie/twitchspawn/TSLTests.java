package net.programmer.igoodie.twitchspawn;

import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class TSLTests {

    @Test
    @DisplayName("should tokenize words correctly.")
    public void tokenizingTest() throws TSLSyntaxError {
        String[] words = {
                "DROP", "word1", "word2",
                "word3", "word with lots of spaces",
                "word with % escaping",
                "word with arbitrary \\e\\s\\c\\a\\p\\e",
        };

        // Merge words into a TSL rule
        String input = "";
        for (String word : words) {
            word = word.replace(TSLParser.QUOTE + "",
                    TSLParser.ESCAPE_CHAR + "" + TSLParser.QUOTE);

            if (word.contains(" ")) {
                input += TSLParser.QUOTE + word + TSLParser.QUOTE + " ";
            } else {
                input += word + " ";
            }
        }

        // Parse words
        List<String> parsedWords = TSLParser.parseWords(input);

        System.out.println("Valid schema:");
        System.out.println(input);
        System.out.println(parsedWords);

        Assertions.assertIterableEquals(Arrays.asList(words), parsedWords);
    }

    @Test
    @DisplayName("should separate rules correctly.")
    public void ruleSeparatingTest() {
        String[] lines = {
                "# Comment line",
                "DROP minecraft:diamond{display:{Name:\"\\\"SomeName\\\"\"}} # EoL Comment",
                "# Another comment line",
                " ON Twitch Follow",
                "",
                "DROP %item{foo:\"Number#1\"}% # Keep the in-quote hash",
                " ON Twitch Follow",
                "# Another comment",
        };

        String input = String.join("\n", lines);

        List<String> rules = Assertions.assertDoesNotThrow(
                () -> TSLParser.parseRules(input));

        System.out.println(input);
        System.out.println("# ------------------- #");
        System.out.println(rules);

        Assertions.assertEquals(2, rules.size());
    }

}
