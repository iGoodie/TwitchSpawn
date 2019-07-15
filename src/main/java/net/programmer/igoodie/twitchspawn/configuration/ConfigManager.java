package net.programmer.igoodie.twitchspawn.configuration;

import net.minecraftforge.fml.loading.FMLPaths;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;

import java.io.File;

public class ConfigManager {

    public static final File CONFIG_FOLDER = new File(FMLPaths.CONFIGDIR.get().toString(), "TwitchSpawn");

    public static CredentialsConfig CREDENTIALS;

    public static void loadConfigs() {
        TwitchSpawn.LOGGER.info("Loading configs...");

        if(!CONFIG_FOLDER.exists())
            CONFIG_FOLDER.mkdirs();

        CREDENTIALS = CredentialsConfig.create(CONFIG_FOLDER.toPath() + "\\credentials.toml");

        TwitchSpawn.LOGGER.info("Configs loaded successfully!");
    }

}
