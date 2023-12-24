package net.programmer.igoodie.twitchspawn.forge;


import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.registries.forge.TwitchSpawnArgumentTypesImpl;


@Mod(TwitchSpawn.MOD_ID)
public class TwitchSpawnForge {
    public TwitchSpawnForge() {
		// Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(TwitchSpawn.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        TwitchSpawn.init();

        // Register Registry
        TwitchSpawnArgumentTypesImpl.REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
