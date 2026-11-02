package net.programmer.igoodie.twitchspawn;

import net.programmer.igoodie.twitchspawn.tslanguage.TSLRuleset;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxErrors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class TSLRulesetTests {

    @BeforeAll
    public static void bootstrap() {
        TestResources.bootstrapMinecraft();
    }

    private static EventArguments donation(double amount) {
        EventArguments args = new EventArguments("donation", "streamlabs");
        args.streamerNickname = "iGoodie";
        args.actorNickname = "Redowar";
        args.donationAmount = amount;
        args.donationCurrency = "USD";
        return args;
    }

    @Test
    @DisplayName("should parse the bundled default ruleset against the current Minecraft registries.")
    public void defaultRulesetTest() {
        String script = TestResources.loadAsString("assets/twitchspawn/default/rules.default.tsl");

        TSLRuleset ruleset = Assertions.assertDoesNotThrow(() -> new TSLRuleset(script),
                "Default rules must parse, otherwise first launch fails");

        Assertions.assertNotNull(ruleset.getEventHandler("Donation"));
        Assertions.assertNotNull(ruleset.getEventHandler("Twitch Follow"));
        Assertions.assertFalse(ruleset.getRulesRaw().isEmpty());
    }

    @Test
    @DisplayName("should route events through predicates.")
    public void predicateRoutingTest() throws TSLSyntaxErrors {
        TSLRuleset ruleset = new TSLRuleset("iGoodie", String.join("\n",
                "DROP minecraft:stick 2",
                " ON Donation",
                " WITH amount IN RANGE [0,20]",
                "",
                "DROP minecraft:diamond_block 1",
                " ON Donation",
                " WITH amount >= 100",
                " WITH currency IS USD"
        ));

        Assertions.assertEquals("iGoodie", ruleset.getStreamer());
        Assertions.assertTrue(ruleset.willPerform(donation(5)));
        Assertions.assertTrue(ruleset.willPerform(donation(150)));
        Assertions.assertFalse(ruleset.willPerform(donation(50)), "No rule covers 21..99");

        EventArguments wrongCurrency = donation(150);
        wrongCurrency.donationCurrency = "EUR";
        Assertions.assertFalse(ruleset.willPerform(wrongCurrency));

        EventArguments follow = new EventArguments("follow", "twitch");
        follow.streamerNickname = "iGoodie";
        Assertions.assertFalse(ruleset.willPerform(follow), "No rule listens for follows");
    }

    @Test
    @DisplayName("should parse every action keyword.")
    public void allActionsTest() {
        String script = String.join("\n",
                "DROP minecraft:diamond_sword[enchantments={smite:2,unbreaking:3}] 1 ON Twitch Follow",
                "DROP %minecraft:stick[custom_name=\"Stick of Truth!\"]% 1 ON Twitch Follow",
                "DROP minecraft:cobblestone 200 ON Twitch Follow",
                "SUMMON minecraft:zombie ~ ~+10 ~ ON Twitch Follow",
                "SUMMON minecraft:creeper ~ ~ ~ %{powered:1b}% ON Twitch Follow",
                "THROW main-hand ON Twitch Follow",
                "CLEAR everything ON Twitch Follow",
                "EXECUTE %/gamerule keep_inventory true% %/time set day% ON Twitch Follow",
                "SHUFFLE hotbar ON Twitch Follow",
                "CHANGE main-hand INTO minecraft:diamond ON Twitch Follow",
                "CHANGE slot 21 FROM inventory INTO %stick[custom_name=\"Stick of Truth!\"]% ON Twitch Follow",
                "THROW helmet ON Twitch Follow",
                "THROW off-hand ON Twitch Follow",
                "NOTHING ON Twitch Follow",
                "EITHER DROP stick OR DROP diamond ON Twitch Follow",
                "EITHER CHANCE 90 PERCENT DROP stick OR CHANCE 10 PERCENT DROP diamond ON Twitch Follow",
                "BOTH INSTANTLY DROP stick AND DROP diamond ON Twitch Follow",
                "FOR 3 TIMES DROP stick ON Twitch Follow",
                "WAIT 2 seconds ON Twitch Follow",
                "REFLECT * DROP stick ON Twitch Follow",
                "OS_RUN LOCAL BASH %echo hi% ON Twitch Follow",
                "DROP stick DISPLAYING NOTHING ON Twitch Follow"
        ).replace("\n", "\n\n"); // Rules are separated by blank lines

        Assertions.assertDoesNotThrow(() -> new TSLRuleset(script));
    }

    @ParameterizedTest(name = "rejects: {0}")
    @ValueSource(strings = {
            "DROP minecraft:not_an_item 1 ON Twitch Follow",   // Item validated against the registry
            "DROP %stick{display:{Name:\"\\\"Old\\\"\"}}% 1 ON Twitch Follow", // Pre-1.20.5 NBT item syntax
            "DROP diamond_sword[not_a_component=1] ON Twitch Follow",
            "CHANGE main-hand INTO minecraft:not_an_item ON Twitch Follow",
            "DROP minecraft:stick many ON Twitch Follow",      // Amount must be an integer
            "DROP stick ON Not An Event",                       // Unknown event
            "LAUNCH stick ON Twitch Follow",                    // Unknown action
            "DROP stick ON Donation WITH amount ~= 3",          // Unknown comparator
            "DROP stick ON Donation WITH amount",               // Incomplete predicate
            "DROP stick DISPLAYING %{not:\"an array\"}% ON Twitch Follow",
    })
    public void syntaxErrorTest(String rule) {
        TSLSyntaxErrors errors = Assertions.assertThrows(TSLSyntaxErrors.class, () -> new TSLRuleset(rule));
        Assertions.assertFalse(errors.getErrors().isEmpty());
    }

    @Test
    @DisplayName("should point rules using pre-26.3 item NBT to the component syntax.")
    public void oldItemSyntaxTest() {
        TSLSyntaxErrors errors = Assertions.assertThrows(TSLSyntaxErrors.class,
                () -> new TSLRuleset("CHANGE helmet INTO %slime_ball{display:{Name:\"\\\"Ewwww\\\"\"}}% ON Twitch Follow"));

        Assertions.assertTrue(errors.getErrors().get(0).getMessage().contains("components"),
                errors.getErrors().get(0).getMessage());
    }

    @Test
    @DisplayName("should collect a syntax error per broken rule.")
    public void multipleErrorsTest() {
        String script = String.join("\n",
                "DROP minecraft:not_an_item ON Twitch Follow",
                "",
                "DROP stick ON Twitch Follow",
                "",
                "LAUNCH stick ON Twitch Follow"
        );

        TSLSyntaxErrors errors = Assertions.assertThrows(TSLSyntaxErrors.class, () -> new TSLRuleset(script));
        Assertions.assertEquals(2, errors.getErrors().size());
    }

}
