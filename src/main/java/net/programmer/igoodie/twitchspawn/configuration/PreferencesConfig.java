package net.programmer.igoodie.twitchspawn.configuration;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlParser;
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

    public enum AutoStartEnum {
        DISABLED, ENABLED
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
            CommentedConfig defaultConfig = new TomlParser().parse(defaultScript());

            config.load();

            getSpecs().correct(config, (action, path, incorrectValue, correctedValue) -> {
                TwitchSpawn.LOGGER.info("[preferences.toml] Corrected {} to {}", incorrectValue, correctedValue);
                config.setComment(path, defaultConfig.getComment(path));
            });

//            config.save();
            save(config); // Here to put new line delimiters between entries & keep default comments

            PreferencesConfig preferencesConfig = new PreferencesConfig();
            preferencesConfig.indicatorDisplay = getEnum(config, "indicatorDisplay", IndicatorDisplay.class);
            preferencesConfig.messageDisplay = getEnum(config, "messageDisplay", MessageDisplay.class);
            preferencesConfig.notificationVolume = config.get("notificationVolume");
            preferencesConfig.notificationPitch = config.get("notificationPitch");
            preferencesConfig.notificationDelay = config.getInt("notificationDelay");
            preferencesConfig.autoStart = getEnum(config, "autoStart", AutoStartEnum.class);
            preferencesConfig.chatGlobalCooldown = config.get("chatGlobalCooldown");
            preferencesConfig.chatIndividualCooldown = config.get("chatIndividualCooldown");
            preferencesConfig.chatWarnings = config.get("chatWarnings").equals("enabled");

            config.close();

            return preferencesConfig;

        } catch (IOException e) {
            throw new InternalError("Tried to read from or save to a non-existing file");
        }
    }

    private static void save(CommentedFileConfig config) {
        StringBuilder data = new StringBuilder();

        for (CommentedConfig.Entry entry : config.entrySet()) {
            String[] commentLines = entry.getComment().split("\r?\n");
            String key = entry.getKey();
            Object value = entry.getValue();

            for (String commentLine : commentLines) {
                data.append("#").append(commentLine).append("\r\n");
            }

            data.append(key).append("=");

            // TODO: append other serialization rules, if needed
            if (value instanceof String)
                data.append('"').append(value).append('"');
            else if (value instanceof Enum)
                data.append('"').append(value.toString().toLowerCase()).append('"');
            else
                data.append(value.toString());

            data.append("\r\n\r\n");
        }

        try {
            FileUtils.writeStringToFile(config.getFile(), data.toString(), StandardCharsets.UTF_8);

        } catch (IOException e) {
            throw new InternalError("Failed writing config on " + config.getFile());
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
            return (value == -1.0) || (0.0 <= value && value <= 1.0);
        });
        spec.defineInRange("notificationDelay", 5000, 0, Integer.MAX_VALUE);
        defineEnum(spec, "autoStart", AutoStartEnum.DISABLED, AutoStartEnum.class);
        spec.defineInRange("chatGlobalCooldown", 1000, 0, Integer.MAX_VALUE);
        spec.defineInRange("chatIndividualCooldown", 1000, 0, Integer.MAX_VALUE);
        spec.define("chatWarnings", "disabled", rawValue -> {
            if (!(rawValue instanceof String))
                return false;
            String value = ((String) rawValue);
            return value.equals("enabled") || value.equals("disabled");
        });

        return spec;
    }

    private static <T extends Enum<T>> T getEnum(CommentedFileConfig config, String path, Class<T> enumClass) {
        Object value = config.get(path);

        if (value instanceof String)
            return Enum.valueOf(enumClass, ((String) value).toUpperCase());

        if (enumClass.isInstance(value))
            return enumClass.cast(value);

        return null;
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
    public int notificationDelay;

    public AutoStartEnum autoStart;

    public int chatGlobalCooldown;
    public int chatIndividualCooldown;
    public boolean chatWarnings;

}
