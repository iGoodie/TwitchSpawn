package net.programmer.igoodie.twitchspawn.configuration;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlParser;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.tracer.Platform;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CredentialsConfig {

    public static CredentialsConfig create(String filepath) throws ParsingException {
        return create(new File(filepath));
    }

    public static CredentialsConfig create(File file) throws ParsingException {
        try {
            // File is not there, create an empty file
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            // Read file as a whole string
            String tomlRaw = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

            // Parse and correct
            CommentedConfig parsedConfig = new TomlParser().parse(tomlRaw);
            ObjectConverter converter = new ObjectConverter();
            boolean corrected = correct(parsedConfig, converter);

            // Save if corrected
            if (corrected) {
                String formatted = TomlFormat.instance()
                        .createWriter()//.setIndent(IndentStyle.SPACES_2)
                        .writeToString(parsedConfig);
                FileUtils.writeStringToFile(file, formatted, StandardCharsets.UTF_8);
            }

            // Convert to CredentialsConfig
            CredentialsConfig credentials = new CredentialsConfig();
            credentials.moderatorsMinecraft = parsedConfig.get("moderatorsMinecraft");
            credentials.moderatorsTwitch = parsedConfig.get("moderatorsTwitch");
            credentials.streamers = (List<Streamer>) ((List) parsedConfig.get("streamers")).stream()
                    .map(config -> toStreamer(converter, (Config) config))
                    .collect(Collectors.toList());

            // TODO: Throw exception on duplicated Streamer (Checked on minecraftNick)

            return credentials;

        } catch (IOException e) {
            throw new InternalError("Tried to read from or save to a non-existing file");
        }
    }

    public static boolean correct(CommentedConfig config, ObjectConverter converter) {
        boolean corrected = false;

        corrected |= correctList(config, "moderatorsMinecraft", String.class,
                Arrays.asList("User321", "iGoodie"));

        corrected |= correctList(config, "moderatorsTwitch", String.class,
                Collections.emptyList());

        corrected |= correctStreamers(config, "streamers", converter,
                Arrays.asList(toConfig(converter, new Streamer(1)), toConfig(converter, new Streamer(2))));

        return corrected;
    }

    private static <T> boolean correctList(Config config, String path, Class<T> type, List<T> defaultList) {
        boolean corrected = false;

        Object listObject = config.get(path);

        // Not a List
        if (!(listObject instanceof List)) {
            TwitchSpawn.LOGGER.info("Correcting {}: Changing type to List", path);
            config.set(path, defaultList);
            return true; // Stop earlier
        }

        List<?> list = (List) listObject;

        // Check if elements match given type
        for (int i = list.size() - 1; i >= 0; i--) {
            Object element = list.get(i);

            if (!type.isInstance(element)) {
                TwitchSpawn.LOGGER.info("Correcting {}: Removing invalid element -> {}", path, element);
                list.remove(element);
                corrected = true;
            }
        }

        // No element left, correct it to default
        if (list.size() == 0) {
            TwitchSpawn.LOGGER.info("Correcting {}: Replacing field with default value {}", path, defaultList);
            config.set(path, defaultList);
            corrected = true;
        }

        return corrected;
    }

    private static boolean correctStreamers(Config config, String path, ObjectConverter converter, List<Config> defaultList) {
        boolean corrected = false;

        Object list = config.get(path);

        // Not a List
        if (!(list instanceof List)) {
            TwitchSpawn.LOGGER.info("Correcting {}: Changing type to List", path);
            config.set(path, defaultList);
            return true; // Stop earlier
        }

        List streamers = (List) list;

        // Check if elements match given type
        for (int i = streamers.size() - 1; i >= 0; i--) {
            Object element = streamers.get(i);

            // Element is not a sub-config
            if (!(element instanceof Config)) {
                TwitchSpawn.LOGGER.info("Correcting {}: Removing invalid element -> {} (Type:{})",
                        path, element, element.getClass().getSimpleName());
                streamers.remove(element);
                corrected = true;
                continue;
            }

            Streamer streamer = null;

            try {
                streamer = toStreamer(converter, (Config) element);

            } catch (IllegalArgumentException e) {
                throw new ParsingException("Unknown platform name -> " + ((Config) element).get("platform"));
            }

            // Element is missing at least one field
            if (streamer.twitchNick == null
                    || streamer.minecraftNick == null
                    || streamer.platform == null
                    || streamer.token == null
                    || streamer.tokenChat == null) {
                TwitchSpawn.LOGGER.info("Correcting {}: Streamer on index {} is missing some fields -> {}", path, i, element);
                streamer = Streamer.from(streamer, new Streamer());
                streamers.set(i, toConfig(converter, streamer));
                corrected = true;
            }
        }

        // No element left, correct it to default
        if (streamers.size() == 0) {
            TwitchSpawn.LOGGER.info("Correcting {}: Replacing field with default value {}", path, defaultList);
            List<Config> streamerConfig = defaultList.stream()
                    .map(streamer -> toConfig(converter, streamer))
                    .collect(Collectors.toList());
            config.set(path, streamerConfig);
            corrected = true;
        }

        return corrected;
    }

    private static Config toConfig(ObjectConverter converter, Object object) {
        Config config = TomlFormat.instance().createConfig();
        converter.toConfig(object, config);
        return config;
    }

    private static Streamer toStreamer(ObjectConverter converter, Config config) {
        Streamer streamer = new Streamer();
        converter.toObject(config, streamer);
        return streamer;
    }

    /* ----------------------------------- */

    public static class Streamer {
        public static Streamer from(Streamer other, Streamer defaultStreamer) {
            Streamer created = new Streamer();
            created.minecraftNick = (other.minecraftNick != null ? other : defaultStreamer).minecraftNick;
            created.twitchNick = (other.twitchNick != null ? other : defaultStreamer).twitchNick;
            created.platform = (other.platform != null ? other : defaultStreamer).platform;
            created.token = (other.token != null ? other : defaultStreamer).token;
            created.tokenChat = (other.tokenChat != null ? other : defaultStreamer).tokenChat;
            return created;
        }

        public String minecraftNick = "MC_NICK";
        public String twitchNick = "TWITCH_NICK";
        public Platform platform = Platform.STREAMLABS;
        public String token = "YOUR_TOKEN_HERE";
        public String tokenChat = "YOUR_CHAT_TOKEN_HERE - Can be generated from https://twitchapps.com/tmi/";

        public Streamer() {}

        public Streamer(int number) {
            this.minecraftNick += number;
            this.twitchNick += number;
        }

        @Override
        public String toString() {
            return new StringBuilder("{")
                    .append("minecraftNick=").append(minecraftNick).append(",")
                    .append("twitchNick=").append(twitchNick).append(",")
                    .append("platform=").append(platform).append(",")
                    .append("token=").append(token != null ? token.replaceAll("\\w", "#") : null)
                    .append("tokenChat=").append(tokenChat != null ? tokenChat.replaceAll("\\w", "#") : null)
                    .append("}")
                    .toString();
        }
    }

    /* ----------------------------------- */

    public List<String> moderatorsMinecraft;
    public List<String> moderatorsTwitch;
    public List<Streamer> streamers;

    public boolean hasPermission(String nickname) {
        if (nickname.equals("@")) // Command block
            return true;

        if (nickname.equalsIgnoreCase("Server")) // Dedicated server
            return true;

        if (moderatorsMinecraft.stream()
                .anyMatch(mod -> mod.equalsIgnoreCase(nickname)))
            return true;

        if (streamers.stream()
                .anyMatch(streamer -> streamer.minecraftNick.equalsIgnoreCase(nickname)))
            return true;

        return false;
    }

}
