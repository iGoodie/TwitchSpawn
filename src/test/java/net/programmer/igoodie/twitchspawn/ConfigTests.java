package net.programmer.igoodie.twitchspawn;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlParser;
import net.programmer.igoodie.twitchspawn.configuration.CredentialsConfig;
import net.programmer.igoodie.twitchspawn.configuration.SubtitlesConfig;
import net.programmer.igoodie.twitchspawn.configuration.TitlesConfig;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLActionKeyword;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLEventKeyword;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

public class ConfigTests {

    @Test
    @DisplayName("should generate default JSON configs correctly.")
    public void defaultJSONGenerationTest() {
        File titlesFile = new File("C:\\Users\\Public\\titles.json");
        File subtitlesFile = new File("C:\\Users\\Public\\subtitles.json");

        TitlesConfig titlesConfig = TitlesConfig.create(titlesFile);
        SubtitlesConfig subtitlesConfig = SubtitlesConfig.create(subtitlesFile);

        // Test for titles
        for (TSLEventKeyword event : TSLEventKeyword.values()) {
            String eventName = event.name().replace("_", " ");
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

}
