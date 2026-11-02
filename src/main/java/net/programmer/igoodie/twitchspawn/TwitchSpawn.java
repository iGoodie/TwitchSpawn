package net.programmer.igoodie.twitchspawn;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.programmer.igoodie.twitchspawn.command.RulesetNameArgumentType;
import net.programmer.igoodie.twitchspawn.command.StreamerArgumentType;
import net.programmer.igoodie.twitchspawn.command.TSLWordsArgumentType;
import net.programmer.igoodie.twitchspawn.command.TwitchSpawnCommand;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.configuration.PreferencesConfig;
import net.programmer.igoodie.twitchspawn.network.NetworkManager;
import net.programmer.igoodie.twitchspawn.network.packet.StatusChangedPacket;
import net.programmer.igoodie.twitchspawn.tracer.TraceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TwitchSpawn implements ModInitializer {

    public static final String MOD_ID = "twitchspawn";
    public static final Logger LOGGER = LogManager.getLogger(TwitchSpawn.class);

    public static final SoundEvent POP_IN = registerSound("pop_in");
    public static final SoundEvent POP_OUT = registerSound("pop_out");

    public static MinecraftServer SERVER;
    public static TraceManager TRACE_MANAGER;

    // Errors from the initial config load, shown to the player once they join a world
    public static TwitchSpawnLoadingErrors LOADING_ERRORS;

    @Override
    public void onInitialize() {
        try {
            ConfigManager.loadConfigs();

        } catch (TwitchSpawnLoadingErrors e) {
            e.logErrors();
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
                throw new RuntimeException("TwitchSpawn loading errors occurred");
            }
            LOADING_ERRORS = e;
        }

        NetworkManager.initialize();

        ArgumentTypeRegistry.registerArgumentType(id("streamer"), StreamerArgumentType.class,
                SingletonArgumentInfo.contextFree(StreamerArgumentType::streamerNick));
        ArgumentTypeRegistry.registerArgumentType(id("ruleset"), RulesetNameArgumentType.class,
                SingletonArgumentInfo.contextFree(RulesetNameArgumentType::rulesetName));
        ArgumentTypeRegistry.registerArgumentType(id("tsl_words"), TSLWordsArgumentType.class,
                SingletonArgumentInfo.contextFree(TSLWordsArgumentType::tslWords));

        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> TwitchSpawnCommand.register(dispatcher));

        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerAboutToStart);
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerLoggedIn(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> onPlayerLoggedOut(handler.player));
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    private static SoundEvent registerSound(String name) {
        Identifier id = id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    private void onServerAboutToStart(MinecraftServer server) {
        SERVER = server;
        TRACE_MANAGER = new TraceManager();
    }

    private void onServerStarted(MinecraftServer server) {
        if (ConfigManager.PREFERENCES.autoStart == PreferencesConfig.AutoStartEnum.ENABLED) {
            LOGGER.info("Auto-start is enabled. Attempting to start tracers.");
            TRACE_MANAGER.start();
        }
    }

    private void onServerStopping(MinecraftServer server) {
        SERVER = null;

        if (TRACE_MANAGER.isRunning())
            TRACE_MANAGER.stop(null, "Server stopping");

        ConfigManager.RULESET_COLLECTION.clearQueue();
    }

    private void onPlayerLoggedIn(ServerPlayer entity) {
        String translationKey = TRACE_MANAGER.isRunning() ?
                "commands.twitchspawn.status.on" : "commands.twitchspawn.status.off";

        entity.sendSystemMessage(Component.translatable(translationKey));

        if (TRACE_MANAGER.isRunning())
            TRACE_MANAGER.connectStreamer(entity.getName().getString());

        NetworkManager.sendTo(entity, new StatusChangedPacket(TRACE_MANAGER.isRunning()));
    }

    private void onPlayerLoggedOut(ServerPlayer entity) {
        if (TRACE_MANAGER.isRunning())
            TRACE_MANAGER.disconnectStreamer(entity.getName().getString());
    }

}
