package net.programmer.igoodie.twitchspawn.configuration;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.toml.TomlFormat;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CredentialsConfig {

    public static CredentialsConfig create(String filepath) throws ParsingException {
        ObjectConverter converter = new ObjectConverter();

        ConfigSpec spec = specs(converter);

        FileConfig config = FileConfig
                .builder(filepath)
                .charset(StandardCharsets.UTF_8)
                .build();

        config.load(); // Throws ParsingException

        int correctionCount = spec.correct(config, (action, path, incorrectVal, correctedVal) -> {
            TwitchSpawn.LOGGER.info("Corrected {} from {} to {}", String.join(".", path), incorrectVal, correctedVal);
        });

        if (correctionCount != 0)
            TwitchSpawn.LOGGER.info("{} correction(s) were made to {}", correctionCount, filepath);

        CredentialsConfig credentials = converter
                .toObject(config, CredentialsConfig::new);

        config.save();
        config.close();

        return credentials;
    }

    private static ConfigSpec specs(ObjectConverter converter) {
        ConfigSpec spec = new ConfigSpec();

        List<CommentedConfig> streamersArray = new ArrayList<>();
        streamersArray.add(subconfig(converter, Streamer.class, 1));
        streamersArray.add(subconfig(converter, Streamer.class, 2));

        // Define specs
        spec.defineList("streamers", streamersArray, e -> e instanceof Config);
        spec.defineList("moderatorsMinecraft", arrayList("User123", "iGoodie"), e -> e instanceof String);
        spec.defineList("moderatorsTwitch", arrayList(), e -> e instanceof String);

        return spec;
    }

    private static CommentedConfig subconfig(ObjectConverter converter, Class<?> pojoClass, int number) {
        CommentedConfig subconfig = TomlFormat.newConfig();

        try {
            Constructor<?> constructor = pojoClass.getConstructor(int.class);
            converter.toConfig(constructor.newInstance(number), subconfig);

        } catch (NoSuchMethodException | InstantiationException
                | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return subconfig;
    }

    private static <T> ArrayList<T> arrayList(T... elements) {
        ArrayList<T> list = new ArrayList<>();
        list.addAll(Arrays.asList(elements));
        return list;
    }

    /* ----------------------------------- */

    public ArrayList<Streamer> streamers;
    public ArrayList<String> moderatorsMinecraft;
    public ArrayList<String> moderatorsTwitch;

    public static class Streamer {
        public String minecraftNick = "MC_NICK";
        public String twitchNick = "TWITCH_NICK";
        public String accessToken = "ACCESS_TOKEN";
        public String socketToken = "SOCKET_TOKEN";

        public Streamer() {}

        public Streamer(int number) {
            this.minecraftNick += number;
            this.twitchNick += number;
            this.accessToken += number;
            this.socketToken += number;
        }
    }

}
