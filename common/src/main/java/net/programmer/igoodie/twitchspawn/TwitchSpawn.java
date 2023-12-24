package net.programmer.igoodie.twitchspawn;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingStage;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.programmer.igoodie.twitchspawn.client.gui.GlobalChatCooldownOverlay;
import net.programmer.igoodie.twitchspawn.client.gui.StatusIndicatorOverlay;
import net.programmer.igoodie.twitchspawn.command.TwitchSpawnCommand;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.configuration.PreferencesConfig;
import net.programmer.igoodie.twitchspawn.network.NetworkManager;
import net.programmer.igoodie.twitchspawn.network.packet.StatusChangedPacket;
import net.programmer.igoodie.twitchspawn.registries.TwitchSpawnAugmentTypes;
import net.programmer.igoodie.twitchspawn.registries.TwitchSpawnSoundEvent;
import net.programmer.igoodie.twitchspawn.tracer.TraceManager;
import net.programmer.igoodie.twitchspawn.udl.NotepadUDLUpdater;

@Mod(TwitchSpawn.MOD_ID)
public class TwitchSpawn {

    public static final String MOD_ID = "twitchspawn";
    public static final Logger LOGGER = LogManager.getLogger(TwitchSpawn.class);

    public static MinecraftServer SERVER;
    public static TraceManager TRACE_MANAGER;

    public TwitchSpawn() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::dedicatedServerSetup);

        MinecraftForge.EVENT_BUS.register(this);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        TwitchSpawnSoundEvent.REGISTRY.register(modEventBus);
        TwitchSpawnAugmentTypes.REGISTRY.register(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        try {
            ConfigManager.loadConfigs();
            NetworkManager.initialize();
        } catch (TwitchSpawnLoadingErrors e) {
            e.bindFMLWarnings(ModLoadingStage.COMMON_SETUP);
            if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
                throw new RuntimeException("TwitchSpawn loading errors occurred");
            }
        }
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        NotepadUDLUpdater.attemptUpdate();
        MinecraftForge.EVENT_BUS.register(StatusIndicatorOverlay.class);
        MinecraftForge.EVENT_BUS.register(GlobalChatCooldownOverlay.class);
    }

    private void dedicatedServerSetup(final FMLDedicatedServerSetupEvent event) {}


    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        TwitchSpawnCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerAboutToStart(ServerAboutToStartEvent event) {
        SERVER = event.getServer();
        TRACE_MANAGER = new TraceManager();
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        if (ConfigManager.PREFERENCES.autoStart == PreferencesConfig.AutoStartEnum.ENABLED) {
            LOGGER.info("Auto-start is enabled. Attempting to start tracers.");
            TRACE_MANAGER.start();
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        SERVER = null;

        if (TRACE_MANAGER.isRunning())
            TRACE_MANAGER.stop(null, "Server stopping");

        ConfigManager.RULESET_COLLECTION.clearQueue();
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer entity = (ServerPlayer) event.getEntity();

        String translationKey = TRACE_MANAGER.isRunning() ?
                "commands.twitchspawn.status.on" : "commands.twitchspawn.status.off";

        entity.sendSystemMessage(Component.translatable(translationKey));

        if (TRACE_MANAGER.isRunning())
            TRACE_MANAGER.connectStreamer(entity.getName().getString());

        NetworkManager.CHANNEL.send(new StatusChangedPacket(TRACE_MANAGER.isRunning()),
                entity.connection.getConnection());
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerPlayer entity = (ServerPlayer) event.getEntity();

        if (TRACE_MANAGER.isRunning())
            TRACE_MANAGER.disconnectStreamer(entity.getName().getString());
    }

}
