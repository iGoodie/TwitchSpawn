package net.programmer.igoodie.twitchspawn;

import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLRuleTokenizer;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLTokenizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class TSLTests {

    @Test
    @DisplayName("should tokenize words correctly.")
    public void tokenizeWordsTest() throws TSLSyntaxError {
        String[] expected = TestResources.loadAsString("comment_expected.tsl").split("\\R");
        String script = TestResources.loadAsString("comment_test.tsl");

        TSLTokenizer tokenizer = new TSLTokenizer(script);
        tokenizer.intoRules(); // Tokenize into rules first

        tokenizer.intoWords(0).forEach(System.out::println);
    }

    @Test
    @DisplayName("should tokenize rules correctly.")
    public void tokenizeRulesTest() {
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
                () -> new TSLTokenizer(input).intoRules());

        System.out.println(input);
        System.out.println("# ------------------- #");
        System.out.println(rules);

        Assertions.assertEquals(2, rules.size());
        Assertions.assertEquals("DROP minecraft:diamond{display:{Name:\"\\\"SomeName\\\"\"}}" +
                " ON Twitch Follow", rules.get(0));
        Assertions.assertEquals("DROP %item{foo:\"Number#1\"}%" +
                " ON Twitch Follow", rules.get(1));
    }

    @Test
    @DisplayName("should trim comments correctly.")
    public void trimCommentsTest() throws TSLSyntaxError {
        String[] expected = TestResources.loadAsString("comment_expected.tsl").split("\\R");
        String script = TestResources.loadAsString("comment_test.tsl");

        TSLTokenizer tokenizer = new TSLTokenizer(script);
        List<String> actual = tokenizer.intoRules();

        for (int i = 0; i < expected.length; i++) {
            System.out.println("Expected -> " + expected[i]);
            System.out.println("Found:   -> " + actual.get(i));
            System.out.println();
        }

        Assertions.assertIterableEquals(Arrays.asList(expected), actual);
    }

    @Test
    public void parseRuleTest() throws TSLSyntaxError {
        List<String> words = TSLTokenizer.intoWords("\n" +
                "EITHER\n" +
                " BOTH INSTANTLY DROP %minecraft:bat_spawn_egg% 2 AND DROP %minecraft:spawner% 1\n" +
                " DISPLAYING %[{text:\"${actor}\", color:\"gold\" }, \" honored you with Bat Spawner!\"]% \n" +
                " OR\n" +
                " BOTH INSTANTLY DROP %minecraft:blaze_spawn_egg% 2 AND DROP %minecraft:spawner% 1\n" +
                " DISPLAYING %[{text:\"${actor}\", color:\"gold\" }, \" cursed you with Blaze Spawner...\"]%\n" +
                " OR\n" +
                " BOTH\n" +
                "  DROP stick\n" +
                "  DISPLAYING %[\"Slooooowly dropping first stick\"]%\n" +
                "  AND\n" +
                "  DROP stick\n" +
                "  DISPLAYING %[\"Slooooowly dropping second stick\"]%\n" +
                " ON Streamlabs Donation\n" +
                " ");
        TSLRuleTokenizer parser = new TSLRuleTokenizer(words);

        parser.intoParts();

        System.out.printf("Action Name:   %s\n", parser.getActionName());
        System.out.printf("Action Params: %s\n", parser.getActionParameters());
        System.out.printf("Event Name:    %s\n", parser.getEventName());
        System.out.printf("Predicates:    %s\n", parser.getPredicateParameters());

        String part1 = "DROP %minecraft:blaze_spawn_egg% 2 AND DROP %minecraft:spawner% 1\n" +
                " DISPLAYING %[{text:\"${actor}\", color:\"gold\" }, \" cursed you with Blaze Spawner...\"]%";

        System.out.println();
        System.out.println("Extracting message JSON from\n" + part1);
        System.out.println(TSLParser.parseMessage(TSLTokenizer.intoWords(part1)));
    }

}
