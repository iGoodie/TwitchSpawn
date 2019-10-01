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

    public enum MessageDisplay {
        DISABLED, TITLES, CHAT
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
            preferencesConfig.indicatorDisplay = getEnum(config, "indicatorDisplay", IndicatorDisplay.class);
            preferencesConfig.messageDisplay = getEnum(config, "messageDisplay", MessageDisplay.class);
            preferencesConfig.notificationVolume = config.get("notificationVolume");
            preferencesConfig.notificationPitch = config.get("notificationPitch");

            config.close();

            return preferencesConfig;

        } catch (IOException e) {
            throw new InternalError("Tried to read from or save to a non-existing file");
        }
    }

    private static ConfigSpec getSpecs() {
        ConfigSpec spec = new ConfigSpec();

//        spec.defineEnum("indicatorDisplay", IndicatorDisplay.ENABLED, EnumGetMethod.NAME_IGNORECASE);
//        spec.defineEnum("messageDisplay", MessageDisplay.TITLES, EnumGetMethod.NAME_IGNORECASE);
        defineEnum(spec, "indicatorDisplay", IndicatorDisplay.ENABLED, IndicatorDisplay.class);
        defineEnum(spec, "messageDisplay", MessageDisplay.TITLES, MessageDisplay.class);
        spec.defineInRange("notificationVolume", 1.0, 0.0, 1.0);
        spec.define("notificationPitch", 1.0, rawValue -> {
            if (!(rawValue instanceof Number))
                return false;
            double value = ((Number) rawValue).doubleValue();
            return value == -1.0 || 0.0 <= value && value <= 1.0;
        });

        return spec;
    }

    private static <T extends Enum<T>> T getEnum(CommentedFileConfig config, String path, Class<T> enumClass) {
        System.out.printf("%s = %s\n", path, config.get(path));
        return Enum.valueOf(enumClass, config.<String>get(path).toUpperCase());
    }

    private static <T extends Enum<T>> void defineEnum(ConfigSpec spec, String path, T defaultValue, Class<T> enumClass) {
        spec.define(path, defaultValue, o -> {
            if (!(o instanceof String))
                return false;

            String enumName = ((String) o).toUpperCase();

            try { Enum.valueOf(enumClass, enumName); } catch (IllegalArgumentException e) {
                return false;
            }

            return true;
        });
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

    public MessageDisplay messageDisplay;

    public double notificationVolume;
    public double notificationPitch;

}
