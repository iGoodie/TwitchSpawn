package net.programmer.igoodie.twitchspawn;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.programmer.igoodie.twitchspawn.client.gui.GlobalChatCooldownOverlay;
import net.programmer.igoodie.twitchspawn.client.gui.StatusIndicatorOverlay;
import net.programmer.igoodie.twitchspawn.command.ItemDataCommand;
import net.programmer.igoodie.twitchspawn.command.TwitchSpawnCommand;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.configuration.PreferencesConfig;
import net.programmer.igoodie.twitchspawn.network.NetworkManager;
import net.programmer.igoodie.twitchspawn.network.packet.StatusChangedPacket;
import net.programmer.igoodie.twitchspawn.tracer.TraceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod.EventBusSubscriber
@Mod(modid = TwitchSpawn.MOD_ID, name = "TwitchSpawn", version = TwitchSpawn.MOD_VERSION)
public class TwitchSpawn {

    public static final String MOD_ID = "twitchspawn";
    public static final String MOD_VERSION = "${mod_version}";
    public static final Logger LOGGER = LogManager.getLogger(TwitchSpawn.class);

    public static MinecraftServer SERVER;
    public static TraceManager TRACE_MANAGER;

    @Mod.EventHandler
    public void preInit(final FMLPreInitializationEvent event) {
        if (event.getSide() == Side.CLIENT) {
            MinecraftForge.EVENT_BUS.register(StatusIndicatorOverlay.class);
            MinecraftForge.EVENT_BUS.register(GlobalChatCooldownOverlay.class);
        }

        NetworkManager.initialize();

        // Set configurations folder path
        String configsFolder = event.getModConfigurationDirectory().getPath();
        ConfigManager.CONFIGS_DIR_PATH = configsFolder;
        ConfigManager.TWITCH_SPAWN_CONFIG_DIR_PATH = configsFolder + File.separator + "TwitchSpawn";

        LOGGER.info("preInit()");
    }

    @Mod.EventHandler
    public void init(final FMLInitializationEvent event) {
        LOGGER.info("init()");
    }

    @Mod.EventHandler
    public void postInit(final FMLPostInitializationEvent event) {
        try { ConfigManager.loadConfigs(); } catch (TwitchSpawnLoadingErrors e) {
            e.display();
        }

        LOGGER.info("postInit()");
    }

    @Mod.EventHandler
    public void onServerAboutToStart(FMLServerAboutToStartEvent event) {
        SERVER = event.getServer();
        TRACE_MANAGER = new TraceManager();
        LOGGER.info("onServerAboutToStart()");
    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new TwitchSpawnCommand());
        event.registerServerCommand(new ItemDataCommand());

        if (ConfigManager.PREFERENCES.autoStart == PreferencesConfig.AutoStartEnum.ENABLED) {
            LOGGER.info("Auto-start is enabled. Attempting to start tracers.");
            TRACE_MANAGER.start();
        }

        LOGGER.info("onServerStarting()");
    }

    @Mod.EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {
        SERVER = null;

        if (TRACE_MANAGER.isRunning())
            TRACE_MANAGER.stop(null, "Server stopping");

        ConfigManager.RULESET_COLLECTION.clearQueue();
        LOGGER.info("onServerStopping()");
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayerMP entity = (EntityPlayerMP) event.player;

        String translationKey = TRACE_MANAGER.isRunning() ?
                "commands.twitchspawn.status.on" : "commands.twitchspawn.status.off";

        entity.sendMessage(new TextComponentTranslation(translationKey));

        NetworkManager.CHANNEL.sendTo(
                new StatusChangedPacket.Message(TRACE_MANAGER.isRunning()),
                entity);

        if (TRACE_MANAGER.isRunning())
            TRACE_MANAGER.connectStreamer(entity.getName());

        LOGGER.info("onPlayerLoggedIn()");
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        EntityPlayerMP entity = (EntityPlayerMP) event.player;

        if (TRACE_MANAGER.isRunning())
            TRACE_MANAGER.disconnectStreamer(entity.getName());

        LOGGER.info("onPlayerLoggedOut()");
    }

}
