package net.programmer.igoodie.twitchspawn;

import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingStage;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.programmer.igoodie.twitchspawn.client.gui.GlobalChatCooldownOverlay;
import net.programmer.igoodie.twitchspawn.client.gui.StatusIndicatorOverlay;
import net.programmer.igoodie.twitchspawn.command.RulesetNameArgumentType;
import net.programmer.igoodie.twitchspawn.command.StreamerArgumentType;
import net.programmer.igoodie.twitchspawn.command.TwitchSpawnCommand;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.log.TSLogger;
import net.programmer.igoodie.twitchspawn.network.NetworkManager;
import net.programmer.igoodie.twitchspawn.udl.NotepadUDLUpdater;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Mod(TwitchSpawn.MOD_ID)
public class TwitchSpawn {

    public static final String MOD_ID = "twitchspawn";
    public static final TSLogger LOGGER = TSLogger.createConsoleLogger(TwitchSpawn.class);
    private static final Map<String, TSLogger> STREAMER_LOGGERS = new HashMap<>(); // Maps lowercase nicks to TSLogger
    public static MinecraftServer SERVER;

    public TwitchSpawn() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::dedicatedServerSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    public static TSLogger getStreamerLogger(String nickname) {
        return STREAMER_LOGGERS.computeIfAbsent(nickname.toLowerCase(),
                (key) -> {
                    try {
                        return TSLogger.createFileLogger(key);
                    } catch (IOException e) {
                        return TwitchSpawn.LOGGER;
                    }
                }
        );
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        try {
            TSLogger.clearHistoricalLogs(14);
            ConfigManager.loadConfigs();
            NetworkManager.initialize();

            ArgumentTypes.register("twitchspawn:streamer", StreamerArgumentType.class,
                    new ArgumentSerializer<>(StreamerArgumentType::streamerNick));
            ArgumentTypes.register("twitchspawn:ruleset", RulesetNameArgumentType.class,
                    new ArgumentSerializer<>(RulesetNameArgumentType::rulesetName));

        } catch (TwitchSpawnLoadingErrors e) {
            e.bindFMLWarnings(ModLoadingStage.COMMON_SETUP);
            e.printStackTrace();
            throw new RuntimeException("TwitchSpawn loading errors occurred");
        }
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        NotepadUDLUpdater.attemptUpdate();
        TwitchSpawnClient.registerKeybinds(event);
        MinecraftForge.EVENT_BUS.register(StatusIndicatorOverlay.class);
        MinecraftForge.EVENT_BUS.register(GlobalChatCooldownOverlay.class);
    }

    private void dedicatedServerSetup(final FMLDedicatedServerSetupEvent event) {}

    @SubscribeEvent
    public void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().register(new SoundEvent(new ResourceLocation(TwitchSpawn.MOD_ID, "pop_in")));
        event.getRegistry().register(new SoundEvent(new ResourceLocation(TwitchSpawn.MOD_ID, "pop_out")));
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        TwitchSpawnCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerAboutToStart(FMLServerAboutToStartEvent event) {
        SERVER = event.getServer();
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) { }

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        SERVER = null;
        ConfigManager.RULESET_COLLECTION.clearQueue();
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) { }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) { }

}
