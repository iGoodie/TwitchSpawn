package net.programmer.igoodie.twitchspawn;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.programmer.igoodie.twitchspawn.command.TwitchSpawnCommand;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.tracer.StreamlabsSocketClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(TwitchSpawn.MOD_ID)
public class TwitchSpawn {

    public static final String MOD_ID = "twitchspawn";
    public static final Logger LOGGER = LogManager.getLogger(TwitchSpawn.class);

    public static MinecraftServer SERVER;

    public TwitchSpawn() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::dedicatedServerSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        try {
            ConfigManager.loadConfigs();

        } catch (TwitchSpawnLoadingErrors e) {
            e.bindFMLWarnings(ModLoadingStage.COMMON_SETUP);
            throw new RuntimeException("TwitchSpawn loading errors occurred");
        }
    }

    private void clientSetup(final FMLClientSetupEvent event) {}

    private void dedicatedServerSetup(final FMLDedicatedServerSetupEvent event) {}

    @SubscribeEvent
    public void onServerAboutToStart(FMLServerAboutToStartEvent event) {
        SERVER = event.getServer();
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        TwitchSpawnCommand.register(event.getCommandDispatcher());
    }

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        SERVER = null;

        if (StreamlabsSocketClient.isRunning()) {
            StreamlabsSocketClient.stop(null, "Server stopping");
        }

        ConfigManager.RULESET_COLLECTION.cleanQueue();
    }

}
