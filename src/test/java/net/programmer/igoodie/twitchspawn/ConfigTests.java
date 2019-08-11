package net.programmer.igoodie.twitchspawn;

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
    @DisplayName("should generate default configs correctly.")
    public void defaultGenerationTest() {
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

}
