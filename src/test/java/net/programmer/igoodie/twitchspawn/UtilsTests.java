package net.programmer.igoodie.twitchspawn;

import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.util.JSONUtils;
import net.programmer.igoodie.twitchspawn.util.PercentageRandomizer;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class UtilsTests {

    @Test
    @DisplayName("should parse percentages correctly.")
    public void percentageParserTest() {
        String[] invalid = {"9999.99999999", ".0", "11111"};
        String[] numbers = {"100", "99.98", "0.0"};
        Integer[] expected = {100_00, 99_98, 0};
        Integer[] actual = Stream.of(numbers)
                .map(TSLParser::parsePercentage)
                .toArray(Integer[]::new);

        Assertions.assertArrayEquals(expected, actual);
        for (String i : invalid) {
            Assertions.assertThrows(IllegalArgumentException.class, () -> TSLParser.parsePercentage(i));
        }
    }

    @Test
    @DisplayName("should randomize percentages correctly.")
    public void percentageRandomizerTest() {
        PercentageRandomizer<String> randomizer = new PercentageRandomizer<>();
        randomizer.addElement("Apple", "90.00");
        randomizer.addElement("Banana", "5.00");
        randomizer.addElement("Pineapple", "4.00");
        randomizer.addElement("Salmon", "1.00");

        System.out.println(randomizer);
        System.out.printf("%s total\n", randomizer.getTotalPercentage() / 100 + "%");

        Map<String, Integer> occurrences = new HashMap<>();

        randomizer.elements().forEach(e -> occurrences.put(e, 0));

        for (int i = 0; i < 100; i++) {
            String s = randomizer.randomItem();
            Integer occurred = occurrences.get(s);
            occurrences.put(s, occurred + 1);
        }

        occurrences.forEach((item, occurred) -> {
            System.out.printf("%s = %d/100\n", item, occurred);
        });
    }

    @Test
    @DisplayName("should not crash randomizing with total < 100%.")
    public void pencentageBelow100Test() {
        PercentageRandomizer<String> randomizer = new PercentageRandomizer<>();
        randomizer.addElement("Apple", "50.00");
        randomizer.addElement("Banana", "5.00");

        System.out.println(randomizer);
        System.out.printf("%s total\n", randomizer.getTotalPercentage() / 100 + "%");

        Map<String, Integer> occurrences = new HashMap<>();

        randomizer.elements().forEach(e -> occurrences.put(e, 0));

        for (int i = 0; i < 100; i++) {
            String s = randomizer.randomItem();
            Integer occurred = occurrences.get(s);
            occurrences.put(s, occurred + 1);
        }

        occurrences.forEach((item, occurred) -> {
            System.out.printf("%s = %d/100\n", item, occurred);
        });
    }

    @Test
    @DisplayName("should escape JSON strings successfully.")
    public void jsonStringEscapistTest() throws JSONException {
        // !drop " \" \ \\ \' \\' \\\
        String original = "!drop \" \\\" \\ \\\\ \\' \\\\' \\\\\\";
        String escaped = JSONUtils.escape(original);

        String jsonString = String.format("{text:'%s'}", escaped);
        System.out.println("Original : " + original);
        System.out.println("Formatted: " + escaped);

        JSONObject json = new JSONObject(jsonString);
        System.out.println("Parsed: " + json);
        System.out.println("Text  : " + json.getString("text"));
    }

}
