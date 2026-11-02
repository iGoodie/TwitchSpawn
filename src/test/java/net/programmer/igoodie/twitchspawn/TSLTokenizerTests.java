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

public class TSLTokenizerTests {

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

        List<String> actual = new TSLTokenizer(script).intoRules();

        Assertions.assertIterableEquals(Arrays.asList(expected), actual);
    }

    @Test
    @DisplayName("should keep %-grouped words together.")
    public void groupedWordsTest() throws TSLSyntaxError {
        List<String> words = TSLTokenizer.intoWords("DROP %minecraft:stick{display:{Name:\"\\\"A B\\\"\"}}% 2 ON Twitch Follow");

        Assertions.assertEquals(Arrays.asList(
                "DROP", "minecraft:stick{display:{Name:\"\\\"A B\\\"\"}}", "2", "ON", "Twitch", "Follow"
        ), words);
    }

    @Test
    @DisplayName("should split a rule into action, event and predicate parts.")
    public void ruleTokenizerTest() throws TSLSyntaxError {
        List<String> words = TSLTokenizer.intoWords("DROP stick 2 ON Donation WITH amount IN RANGE [0,20] WITH currency = USD");
        TSLRuleTokenizer parts = new TSLRuleTokenizer(words).intoParts();

        Assertions.assertEquals("DROP", parts.getActionName());
        Assertions.assertEquals(Arrays.asList("stick", "2"), parts.getActionParameters());
        Assertions.assertEquals("Donation", parts.getEventName());
        Assertions.assertEquals(Arrays.asList(
                Arrays.asList("amount", "IN", "RANGE", "[0,20]"),
                Arrays.asList("currency", "=", "USD")
        ), parts.getPredicateParameters());
    }

    @Test
    @DisplayName("should extract the DISPLAYING message as a JSON array.")
    public void parseMessageTest() throws TSLSyntaxError {
        String part = "DROP %minecraft:spawner% 1" +
                " DISPLAYING %[{text:\"${actor}\", color:\"gold\" }, \" cursed you with Blaze Spawner...\"]%";

        var message = TSLParser.parseMessage(TSLTokenizer.intoWords(part));

        Assertions.assertNotNull(message);
        Assertions.assertEquals(2, message.size());
        Assertions.assertEquals("${actor}", message.get(0).getAsJsonObject().get("text").getAsString());
        Assertions.assertEquals(" cursed you with Blaze Spawner...", message.get(1).getAsString());
    }

    @Test
    @DisplayName("should reject more than one DISPLAYING keyword.")
    public void multipleDisplayingTest() throws TSLSyntaxError {
        List<String> words = TSLTokenizer.intoWords("DROP stick DISPLAYING %[\"a\"]% DISPLAYING %[\"b\"]%");

        Assertions.assertThrows(TSLSyntaxError.class, () -> TSLParser.parseMessage(words));
    }

}
