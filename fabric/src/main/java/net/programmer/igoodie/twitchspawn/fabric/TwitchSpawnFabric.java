package net.programmer.igoodie.twitchspawn.fabric;

import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.fabricmc.api.ModInitializer;


public class TwitchSpawnFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        TwitchSpawn.init();
        TwitchSpawn.LOGGER.info("TwitchSpawn Fabric initialized!");
    }
}