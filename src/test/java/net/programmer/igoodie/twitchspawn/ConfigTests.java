package net.programmer.igoodie.twitchspawn;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlParser;
import net.programmer.igoodie.twitchspawn.configuration.CredentialsConfig;
import net.programmer.igoodie.twitchspawn.configuration.PreferencesConfig;
import net.programmer.igoodie.twitchspawn.configuration.SubtitlesConfig;
import net.programmer.igoodie.twitchspawn.configuration.TitlesConfig;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLActionKeyword;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLEventKeyword;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ConfigTests {

    @Test
    @DisplayName("should generate default JSON configs correctly.")
    public void defaultJSONGenerationTest() {
        File titlesFile = new File(".\\titles.json");
        File subtitlesFile = new File(".\\subtitles.json");

        TitlesConfig titlesConfig = TitlesConfig.create(titlesFile);
        SubtitlesConfig subtitlesConfig = SubtitlesConfig.create(subtitlesFile);

        // Test for titles
        for (TSLEventKeyword event : TSLEventKeyword.values()) {
            String eventName = event.eventName;
            System.out.println("Looking for " + eventName);

            Assertions.assertNotNull(titlesConfig.getTextComponent(eventName));
            Assertions.assertTrue(titlesConfig.getTextComponent(eventName).isJsonArray());
        }

        // Test for subtitles
        for (TSLActionKeyword action : TSLActionKeyword.values()) {
            if (!action.displayable) continue;

            System.out.println("Looking for " + action.name());

            Assertions.assertNotNull(subtitlesConfig.getTextComponent(action.name()));
            Assertions.assertTrue(subtitlesConfig.getTextComponent(action.name()).isJsonArray());
        }

    }

    @Test
    @DisplayName("should generate default TOML configs correctly.")
    public void defaultTOMLGenerationTest() {
        String testToml = TestResources.loadAsString("test.toml");
        CommentedConfig parsedConfig = new TomlParser().parse(testToml);

        System.out.println("# --------- CORRECTING ------------- #");
        CredentialsConfig.correct(parsedConfig, new ObjectConverter());
        System.out.println();

        String formatted = TomlFormat.instance()
                .createWriter()//.setIndent(IndentStyle.SPACES_2)
                .writeToString(parsedConfig);

        System.out.println("# --------- PARSED ------------- #");
        System.out.println(parsedConfig);
        System.out.println();

        System.out.println("# --------- FORMATTED ------------- #");
        System.out.println(formatted);
    }

    @Test
    @DisplayName("should generate and load preferences.toml correctly")
    public void preferencesTOMLTest() {
        File preferencesFile = new File(".\\preferences.toml");

        // Write empty file
        try { FileUtils.writeStringToFile(preferencesFile, "", StandardCharsets.UTF_8); } catch (IOException e) {
            e.printStackTrace();
        }

        // Create preferences out of empty file
        PreferencesConfig preferencesConfig = PreferencesConfig.create(preferencesFile);

        // Assert default values
        Assertions.assertEquals(PreferencesConfig.MessageDisplay.TITLES, preferencesConfig.messageDisplay);
        Assertions.assertEquals(PreferencesConfig.IndicatorDisplay.ENABLED, preferencesConfig.indicatorDisplay);
        Assertions.assertEquals(1.0d, preferencesConfig.notificationVolume);
        Assertions.assertEquals(1.0d, preferencesConfig.notificationPitch);
        Assertions.assertEquals(5000, preferencesConfig.notificationDelay);

        System.out.println("#------- Preferences.toml -------#");
        System.out.println(preferencesConfig.indicatorDisplay);
        System.out.println(preferencesConfig.messageDisplay);
        System.out.println(preferencesConfig.notificationVolume);
        System.out.println(preferencesConfig.notificationPitch);
        System.out.println(preferencesConfig.notificationDelay);
    }

    @AfterAll
    public static void removeConfigFiles() {
        File titlesJSON = new File(".\\titles.json");
        File subtitlesJSON = new File(".\\subtitles.json");
        File preferencesTOML = new File(".\\preferences.toml");
        titlesJSON.delete();
        subtitlesJSON.delete();
        preferencesTOML.delete();
    }

}
