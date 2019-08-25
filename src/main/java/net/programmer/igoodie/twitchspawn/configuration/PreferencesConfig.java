package net.programmer.igoodie.twitchspawn.configuration;

import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.EnumGetMethod;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.io.Resources;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class PreferencesConfig {

    public enum IndicatorDisplay {
        DISABLED, CIRCLE_ONLY, ENABLED
    }

    public static PreferencesConfig create(File file) {
        try {
            // File is not there, create an empty file
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();

                FileUtils.writeStringToFile(file, defaultScript(), StandardCharsets.UTF_8);
            }

            CommentedFileConfig config = CommentedFileConfig.builder(file).build();

            config.load();

            getSpecs().correct(config, (action, path, incorrectValue, correctedValue) -> {
                TwitchSpawn.LOGGER.info("[preferences.toml] Corrected {} to {}", incorrectValue, correctedValue);
            });

            config.save();

            PreferencesConfig preferencesConfig = new PreferencesConfig();
            preferencesConfig.indicatorDisplay = config.getEnum("indicatorDisplay", IndicatorDisplay.class);

            config.close();

            return preferencesConfig;

        } catch (IOException e) {
            throw new InternalError("Tried to read from or save to a non-existing file");
        }
    }

    private static ConfigSpec getSpecs() {
        ConfigSpec spec = new ConfigSpec();

        spec.defineEnum("indicatorDisplay", IndicatorDisplay.ENABLED, EnumGetMethod.NAME_IGNORECASE);

        return spec;
    }

    private static String defaultScript() {
        try {
            URL location = Resources.getResource("assets/twitchspawn/default/preferences.toml");
            return Resources.toString(location, StandardCharsets.UTF_8);

        } catch (IOException e) {
            throw new InternalError("Missing default file: ../assets/twitchspawn/default/preferences.toml");
        }
    }

    /* ---------------------------------- */

    public IndicatorDisplay indicatorDisplay;

}
