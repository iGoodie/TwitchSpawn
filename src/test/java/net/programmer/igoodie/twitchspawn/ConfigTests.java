package net.programmer.igoodie.twitchspawn;

import com.electronwill.nightconfig.core.io.ParsingException;
import com.google.gson.JsonSyntaxException;
import net.programmer.igoodie.twitchspawn.configuration.CredentialsConfig;
import net.programmer.igoodie.twitchspawn.configuration.SubtitlesConfig;
import net.programmer.igoodie.twitchspawn.configuration.TitlesConfig;
import net.programmer.igoodie.twitchspawn.tracer.Platform;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLActionKeyword;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLEventKeyword;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ConfigTests {

    @TempDir
    Path configDir;

    @Test
    @DisplayName("should generate default title and subtitle configs.")
    public void defaultJSONGenerationTest() {
        TitlesConfig titlesConfig = TitlesConfig.create(configDir.resolve("messages.title.json").toFile());
        SubtitlesConfig subtitlesConfig = SubtitlesConfig.create(configDir.resolve("messages.subtitle.json").toFile());

        for (TSLEventKeyword event : TSLEventKeyword.values()) {
            Assertions.assertNotNull(titlesConfig.getTextComponent(event.eventName), "Missing title for " + event.eventName);
        }
        Assertions.assertNotNull(titlesConfig.getTextComponent("reflection"), "Missing title for reflection");

        for (TSLActionKeyword action : TSLActionKeyword.values()) {
            if (!action.displayable) continue;
            Assertions.assertNotNull(subtitlesConfig.getTextComponent(action.name()), "Missing subtitle for " + action.name());
        }

        Assertions.assertTrue(Files.exists(configDir.resolve("messages.title.json")));
        Assertions.assertTrue(Files.exists(configDir.resolve("messages.subtitle.json")));
    }

    @Test
    @DisplayName("should reject malformed message configs.")
    public void malformedJSONTest() throws IOException {
        Path titles = configDir.resolve("messages.title.json");
        Files.writeString(titles, "[\"not an object\"]");

        Assertions.assertThrows(JsonSyntaxException.class, () -> TitlesConfig.create(titles.toFile()));
    }

    @Test
    @DisplayName("should create default credentials when the file is missing.")
    public void defaultCredentialsTest() {
        Path credentialsFile = configDir.resolve("credentials.toml");

        CredentialsConfig credentials = CredentialsConfig.create(credentialsFile.toFile());

        Assertions.assertTrue(Files.exists(credentialsFile));
        Assertions.assertEquals(2, credentials.streamers.size());
        Assertions.assertTrue(credentials.hasPermission("iGoodie"), "Default moderators should have permission");
        Assertions.assertFalse(credentials.hasPermission("SomeRandomViewer"));
    }

    @Test
    @DisplayName("should correct malformed and old-format credentials.")
    public void correctCredentialsTest() throws IOException {
        Path credentialsFile = configDir.resolve("credentials.toml");
        Files.writeString(credentialsFile, TestResources.loadAsString("test.toml"), StandardCharsets.UTF_8);

        CredentialsConfig credentials = CredentialsConfig.create(credentialsFile.toFile());

        Assertions.assertNotNull(credentials.moderatorsMinecraft);
        Assertions.assertNotNull(credentials.moderatorsTwitch);
        Assertions.assertEquals(6, credentials.streamers.size());

        for (CredentialsConfig.Streamer streamer : credentials.streamers) {
            Assertions.assertEquals("iGoodie", streamer.minecraftNick);
            Assertions.assertEquals(Platform.STREAMLABS, streamer.platform);
            Assertions.assertNotNull(streamer.token);
        }

        // Corrected file should load again without needing changes
        String corrected = Files.readString(credentialsFile);
        CredentialsConfig.create(credentialsFile.toFile());
        Assertions.assertEquals(corrected, Files.readString(credentialsFile));
    }

    @Test
    @DisplayName("should reject unknown platforms.")
    public void unknownPlatformTest() throws IOException {
        Path credentialsFile = configDir.resolve("credentials.toml");
        Files.writeString(credentialsFile, String.join("\n",
                "moderatorsMinecraft = [\"iGoodie\"]",
                "moderatorsTwitch = []",
                "[[streamers]]",
                "  minecraftNick = \"iGoodie\"",
                "  twitchNick = \"iGoodiex\"",
                "  platform = \"Myspace\"",
                "  token = \"x\"",
                "  tokenChat = \"y\""
        ));

        Assertions.assertThrows(ParsingException.class, () -> CredentialsConfig.create(credentialsFile.toFile()));
    }

    @Test
    @DisplayName("should map loading errors to translated chat messages.")
    public void loadingErrorsTest() {
        TwitchSpawnLoadingErrors errors = new TwitchSpawnLoadingErrors();
        Assertions.assertTrue(errors.isEmpty());

        errors.addException(new TSLSyntaxError("Bad rule"));
        errors.addException(new ParsingException("Bad toml"));
        errors.addException(new JsonSyntaxException("Bad json"));
        errors.addException(new IllegalStateException("Bad luck"));

        List<String> keys = errors.toWarningMessages().stream()
                .map(component -> ((net.minecraft.network.chat.contents.TranslatableContents) component.getContents()).getKey())
                .toList();

        Assertions.assertEquals(List.of(
                "twitchspawn.error.tsl",
                "twitchspawn.error.toml",
                "twitchspawn.error.json",
                "twitchspawn.error.unknown"
        ), keys);
    }

}
