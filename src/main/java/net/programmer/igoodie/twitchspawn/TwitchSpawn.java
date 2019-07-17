package net.programmer.igoodie.twitchspawn;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
        LOGGER.debug("Executing {}::commonSetup", getClass().getSimpleName());
        ConfigManager.loadConfigs();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.debug("Executing {}::clientSetup", getClass().getSimpleName());
    }

    private void dedicatedServerSetup(final FMLDedicatedServerSetupEvent event) {
        LOGGER.debug("Executing {}::dedicatedServerSetup", getClass().getSimpleName());
    }

    @SubscribeEvent
    public void onServerAboutToStart(FMLServerAboutToStartEvent event) {
        LOGGER.debug("Executing {}::onServerAboutToStart", getClass().getSimpleName());
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        LOGGER.debug("Executing {}::onServerStarting", getClass().getSimpleName());
        TwitchSpawnCommand.register(event.getCommandDispatcher());
        SERVER = event.getServer();
    }

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        LOGGER.debug("Executing {}::onServerStopping", getClass().getSimpleName());
        SERVER = null;
    }

    @SubscribeEvent
    public static void foo(final RegistryEvent.Register<?> registryEvent) {
        LOGGER.info(registryEvent.getRegistry());
    }

}
