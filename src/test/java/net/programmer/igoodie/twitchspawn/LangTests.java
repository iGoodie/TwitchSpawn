package net.programmer.igoodie.twitchspawn;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Forge lang files used {0}-style placeholders, vanilla uses %1$s.
 * These make sure every translation formats with the arguments the code actually passes.
 */
public class LangTests {

    private static final Pattern FORGE_PLACEHOLDER = Pattern.compile("\\{\\d+}");

    // Number of arguments each translation key is given in the code
    private static final Map<String, Integer> ARGUMENT_COUNTS = new HashMap<>();

    static {
        ARGUMENT_COUNTS.put("twitchspawn.error.tsl", 2);
        ARGUMENT_COUNTS.put("twitchspawn.error.json", 2);
        ARGUMENT_COUNTS.put("twitchspawn.error.toml", 2);
        ARGUMENT_COUNTS.put("twitchspawn.error.unknown", 2);
        ARGUMENT_COUNTS.put("commands.twitchspawn.reloadcfg.invalid_syntax", 1);
        ARGUMENT_COUNTS.put("commands.twitchspawn.stop.success", 2);
        ARGUMENT_COUNTS.put("commands.twitchspawn.rules.list", 1);
        ARGUMENT_COUNTS.put("commands.twitchspawn.rules.default", 2);
        ARGUMENT_COUNTS.put("commands.twitchspawn.rules.one", 2);
        ARGUMENT_COUNTS.put("commands.twitchspawn.rules.one.fail", 1);
        ARGUMENT_COUNTS.put("commands.twitchspawn.simulate.invalid_event", 1);
        ARGUMENT_COUNTS.put("commands.twitchspawn.simulate.success", 1);
        ARGUMENT_COUNTS.put("commands.twitchspawn.test.not_found", 1);
        ARGUMENT_COUNTS.put("commands.twitchspawn.test.success", 1);
    }

    private static Language previousLanguage;

    @BeforeAll
    public static void rememberLanguage() {
        previousLanguage = Language.getInstance();
    }

    @AfterAll
    public static void restoreLanguage() {
        Language.inject(previousLanguage);
    }

    private static JsonObject loadLang(String code) {
        String json = TestResources.loadAsString("assets/twitchspawn/lang/" + code + ".json");
        return JsonParser.parseString(json).getAsJsonObject();
    }

    private static void injectLanguage(JsonObject translations) {
        Language.inject(new Language() {
            @Override
            public String getOrDefault(String key, String fallback) {
                return translations.has(key) ? translations.get(key).getAsString() : fallback;
            }

            @Override
            public boolean has(String key) {
                return translations.has(key);
            }

            @Override
            public boolean isDefaultRightToLeft() {
                return false;
            }

            @Override
            public FormattedCharSequence getVisualOrder(FormattedText text) {
                return FormattedCharSequence.EMPTY;
            }
        });
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"en_us", "de_de", "es_es", "fr_fr", "it_it"})
    public void sameKeysAsEnglishTest(String code) {
        JsonObject english = loadLang("en_us");
        JsonObject lang = loadLang(code);

        for (String key : english.keySet()) {
            Assertions.assertTrue(lang.has(key), code + " is missing key " + key);
        }
        for (String key : lang.keySet()) {
            Assertions.assertTrue(english.has(key), code + " has a key unknown to en_us: " + key);
        }
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"en_us", "de_de", "es_es", "fr_fr", "it_it"})
    public void noForgePlaceholdersTest(String code) {
        loadLang(code).entrySet().forEach(entry -> Assertions.assertFalse(
                FORGE_PLACEHOLDER.matcher(entry.getValue().getAsString()).find(),
                code + ":" + entry.getKey() + " still uses a Forge {n} placeholder"));
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"en_us", "de_de", "es_es", "fr_fr", "it_it"})
    public void formatsWithCodeArgumentsTest(String code) {
        JsonObject lang = loadLang(code);
        injectLanguage(lang);

        for (String key : lang.keySet()) {
            int argumentCount = ARGUMENT_COUNTS.getOrDefault(key, 0);
            Object[] args = new Object[argumentCount];
            for (int i = 0; i < argumentCount; i++) args[i] = "<arg" + i + ">";

            String formatted = Component.translatable(key, args).getString();

            // Vanilla falls back to the raw string when a placeholder is invalid or out of range
            Assertions.assertFalse(formatted.matches("(?s).*%(\\d+\\$)?s.*"),
                    code + ":" + key + " did not format -> " + formatted);
        }
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"en_us", "de_de", "es_es", "fr_fr", "it_it"})
    public void placeholdersReceiveArgumentsTest(String code) {
        injectLanguage(loadLang(code));

        String stopped = Component.translatable("commands.twitchspawn.stop.success", "Server", "Testing").getString();
        Assertions.assertTrue(stopped.contains("Server") && stopped.contains("Testing"), stopped);

        String ruleset = Component.translatable("commands.twitchspawn.rules.default", "<name>", "DROP action 1 time(s).").getString();
        Assertions.assertTrue(ruleset.contains("DROP action 1 time(s)."), ruleset);
        Assertions.assertFalse(ruleset.contains("<name>"), "rules.default should only show the ruleset -> " + ruleset);

        String unknown = Component.translatable("twitchspawn.error.unknown", "Boom", "RuntimeException").getString();
        Assertions.assertTrue(unknown.contains("Boom") && unknown.contains("RuntimeException"), unknown);
    }

}
