package net.programmer.igoodie.twitchspawn.configuration;

import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.TwitchSpawnLoadingErrors;
import net.programmer.igoodie.twitchspawn.tslanguage.TSLRulesetCollection;

import java.io.File;

public class ConfigManager {

    public static String CONFIG_DIR_PATH;

    public static CredentialsConfig CREDENTIALS;
    public static TSLRulesetCollection RULESET_COLLECTION;
    public static TitlesConfig TITLES;
    public static SubtitlesConfig SUBTITLES;
    public static PreferencesConfig PREFERENCES;

    public static void loadConfigs(File configsDir) throws TwitchSpawnLoadingErrors {
        TwitchSpawn.LOGGER.info("Loading configs...");
        TwitchSpawnLoadingErrors errors = new TwitchSpawnLoadingErrors();

        CONFIG_DIR_PATH = configsDir.getPath() + File.separator + "TwitchSpawn";

        File configDirectory = new File(CONFIG_DIR_PATH);

        if (!configDirectory.exists())
            configDirectory.mkdirs();

        accumulateExceptions(errors,
                () -> CREDENTIALS = CredentialsConfig.create(getPath("credentials.toml")));
        accumulateExceptions(errors,
                () -> RULESET_COLLECTION = RulesConfig.createRules(CONFIG_DIR_PATH));
        accumulateExceptions(errors,
                () -> TITLES = TitlesConfig.create(new File(getPath("messages.title.json"))));
        accumulateExceptions(errors,
                () -> SUBTITLES = SubtitlesConfig.create(new File(getPath("messages.subtitle.json"))));
        accumulateExceptions(errors,
                () -> PREFERENCES = PreferencesConfig.create(new File(getPath("preferences.toml"))));

        if(!errors.isEmpty())
            throw errors;

        TwitchSpawn.LOGGER.info("Configs loaded successfully!");
    }

    public static String getPath(String relativePath) {
        return CONFIG_DIR_PATH + File.separator + relativePath;
    }

    private static void accumulateExceptions(TwitchSpawnLoadingErrors container, ExceptionAccumulator task) {
        try {
            task.execute();
        } catch (Exception e) {
            container.addException(e);
        }
    }

    @FunctionalInterface
    private interface ExceptionAccumulator {
        void execute() throws Exception;
    }

}
