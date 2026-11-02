package net.programmer.igoodie.twitchspawn;

import net.programmer.igoodie.twitchspawn.eventqueue.EventQueue;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;
import net.programmer.igoodie.twitchspawn.util.CooldownBucket;
import net.programmer.igoodie.twitchspawn.util.ExpressionEvaluator;
import net.programmer.igoodie.twitchspawn.util.JSONUtils;
import net.programmer.igoodie.twitchspawn.util.PercentageRandomizer;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class UtilsTests {

    @Test
    @DisplayName("should parse percentages correctly.")
    public void percentageParserTest() {
        String[] invalid = {"9999.99999999", ".0", "11111"};
        String[] numbers = {"100", "99.98", "0.0", "12.5"};
        Integer[] expected = {100_00, 99_98, 0, 12_50};
        Integer[] actual = Stream.of(numbers)
                .map(TSLParser::parsePercentage)
                .toArray(Integer[]::new);

        Assertions.assertArrayEquals(expected, actual);
        for (String i : invalid) {
            Assertions.assertThrows(IllegalArgumentException.class, () -> TSLParser.parsePercentage(i));
        }
    }

    @Test
    @DisplayName("should only pick elements that were added.")
    public void percentageRandomizerTest() {
        PercentageRandomizer<String> randomizer = new PercentageRandomizer<>();
        randomizer.addElement("Apple", "90.00");
        randomizer.addElement("Banana", "5.00");
        randomizer.addElement("Pineapple", "4.00");
        randomizer.addElement("Salmon", "1.00");

        Assertions.assertEquals(100_00, randomizer.getTotalPercentage());

        for (int i = 0; i < 1000; i++) {
            Assertions.assertTrue(randomizer.elements().contains(randomizer.randomItem()));
        }
    }

    @Test
    @DisplayName("should always pick an element that has 100%.")
    public void percentageRandomizerCertainTest() {
        PercentageRandomizer<String> randomizer = new PercentageRandomizer<>();
        randomizer.addElement("Never", "0.00");
        randomizer.addElement("Always", "100.00");

        for (int i = 0; i < 1000; i++) {
            Assertions.assertEquals("Always", randomizer.randomItem());
        }
    }

    @Test
    @DisplayName("should not crash randomizing with total < 100%.")
    public void percentageBelow100Test() {
        PercentageRandomizer<String> randomizer = new PercentageRandomizer<>();
        randomizer.addElement("Apple", "50.00");
        randomizer.addElement("Banana", "5.00");

        Assertions.assertDoesNotThrow(() -> {
            for (int i = 0; i < 1000; i++) randomizer.randomItem();
        });
    }

    @Test
    @DisplayName("should escape JSON strings successfully.")
    public void jsonStringEscapistTest() throws JSONException {
        // !drop " \" \ \\ \' \\' \\\
        String original = "!drop \" \\\" \\ \\\\ \\' \\\\' \\\\\\";
        String escaped = JSONUtils.escape(original);

        JSONObject json = new JSONObject(String.format("{text:'%s'}", escaped));
        Assertions.assertEquals(original, json.getString("text"));
    }

    @Test
    @DisplayName("should replace ${expressions} from event arguments.")
    public void expressionEvaluatorTest() {
        EventArguments args = new EventArguments("donation", "streamlabs");
        args.streamerNickname = "iGoodie";
        args.actorNickname = "Redowar";
        args.donationAmount = 12.345;
        args.donationCurrency = "USD";
        args.message = "Say \"hi\"";
        args.subscriptionTier = 0;

        String result = ExpressionEvaluator.replaceExpressions(
                "${actor} donated ${amount_f}${currency} (${amount_i}) to ${streamer}: ${message} [${tier}] ${unknown}",
                expression -> {
                    String value = ExpressionEvaluator.fromArgs(expression, args);
                    return value != null ? value : "${" + expression + "}";
                });

        Assertions.assertEquals(String.format(
                "Redowar donated %.2fUSD (12) to iGoodie: Say \\\"hi\\\" [Prime] ${unknown}", 12.345), result);
        Assertions.assertEquals("Say \"hi\"", ExpressionEvaluator.fromArgs("message_unescaped", args));
        Assertions.assertNull(ExpressionEvaluator.fromArgs("months", args), "Unset months must not evaluate");
    }

    @Test
    @DisplayName("should apply global and individual cooldowns.")
    public void cooldownBucketTest() {
        CooldownBucket bucket = new CooldownBucket(60_000, 120_000);

        Assertions.assertFalse(bucket.hasGlobalCooldown(), "Fresh bucket must not be on cooldown");
        Assertions.assertTrue(bucket.canConsume("viewer1"));
        bucket.consume("viewer1");

        Assertions.assertTrue(bucket.hasGlobalCooldown());
        Assertions.assertTrue(bucket.hasCooldown("viewer1"));
        Assertions.assertFalse(bucket.hasCooldown("viewer2"));
        Assertions.assertFalse(bucket.canConsume("viewer2"), "Global cooldown blocks everyone");
        Assertions.assertTrue(bucket.getGlobalCooldown() > 0);
    }

    @Test
    @DisplayName("should not apply a global cooldown when disabled.")
    public void noGlobalCooldownTest() {
        CooldownBucket bucket = new CooldownBucket(0, 120_000);
        bucket.consume("viewer1");

        Assertions.assertFalse(bucket.hasGlobalCooldown());
        Assertions.assertTrue(bucket.canConsume("viewer2"));
        Assertions.assertFalse(bucket.canConsume("viewer1"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    @DisplayName("should run queued tasks in order.")
    public void eventQueueOrderTest() throws InterruptedException {
        EventQueue queue = new EventQueue(0);
        List<Integer> list = Collections.synchronizedList(new LinkedList<>());
        CountDownLatch done = new CountDownLatch(1);

        queue.queue(() -> list.add(1));
        queue.queue(() -> list.add(2));
        queue.queueSleep(50);
        queue.queue(() -> list.add(3));
        queue.queueFirst("First", () -> list.add(0));
        queue.queue(done::countDown);
        queue.updateThread();

        Assertions.assertTrue(done.await(5, TimeUnit.SECONDS), "Queue never finished");
        Assertions.assertIterableEquals(Arrays.asList(0, 1, 2, 3), list);
        Assertions.assertFalse(queue.hasUnhandledEvent());
    }

}
