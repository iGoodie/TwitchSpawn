package net.programmer.igoodie.twitchspawn.configuration;

import net.minecraftforge.fml.loading.FMLPaths;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.tslanguage.TSLRules;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxErrors;

import java.io.File;

public class ConfigManager {

    public static final String CONFIG_DIR_PATH = FMLPaths.CONFIGDIR.get().toString() + File.separator + "TwitchSpawn";

    public static CredentialsConfig CREDENTIALS;
    public static TSLRules HANDLING_RULES;
    public static TitlesConfig TITLES;
    public static SubtitlesConfig SUBTITLES;

    public static void loadConfigs() throws TSLSyntaxErrors {
        TwitchSpawn.LOGGER.info("Loading configs...");

        File configDirectory = new File(CONFIG_DIR_PATH);

        if (!configDirectory.exists())
            configDirectory.mkdirs();

        CREDENTIALS = CredentialsConfig.create(getPath("credentials.toml"));
        HANDLING_RULES = RulesConfig.createRules(CONFIG_DIR_PATH);
        TITLES = TitlesConfig.create(new File(getPath("messages.title.json")));
        SUBTITLES = SubtitlesConfig.create(new File(getPath("messages.subtitle.json")));

        TwitchSpawn.LOGGER.info("Configs loaded successfully!");
    }

    public static String getPath(String relativePath) {
        return CONFIG_DIR_PATH + File.separator + relativePath;
    }

}
