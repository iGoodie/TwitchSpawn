package net.programmer.igoodie.twitchspawn;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.programmer.igoodie.twitchspawn.client.gui.GlobalChatCooldownOverlay;
import net.programmer.igoodie.twitchspawn.client.gui.StatusIndicatorOverlay;
import net.programmer.igoodie.twitchspawn.command.TwitchSpawnCommand;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.configuration.PreferencesConfig;
import net.programmer.igoodie.twitchspawn.events.TwitchSpawnCommonEvent;
import net.programmer.igoodie.twitchspawn.events.TwitchSpawnEventHandler;
import net.programmer.igoodie.twitchspawn.network.NetworkManager;
import net.programmer.igoodie.twitchspawn.network.packet.StatusChangedPacket;
import net.programmer.igoodie.twitchspawn.registries.TwitchSpawnArgumentTypes;
import net.programmer.igoodie.twitchspawn.registries.TwitchSpawnSoundEvent;
import net.programmer.igoodie.twitchspawn.tracer.TraceManager;
import net.programmer.igoodie.twitchspawn.udl.NotepadUDLUpdater;


public class TwitchSpawn
{
    /**
     * The plugin mod-id
     */
	public static final String MOD_ID = "twitchspawn";

    /**
     * Minecraft server instance.
     */
	public static MinecraftServer SERVER;

    /**
     * Trace manager.
     */
	public static TraceManager TRACE_MANAGER;

    /**
     * Logger.
     */
	public static final Logger LOGGER = LogManager.getLogger();


    /**
     * The main init class.
     */
	public static void init()
	{
		TwitchSpawnEventHandler.init();

		CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) ->
			TwitchSpawnCommand.register(dispatcher));

		EnvExecutor.runInEnv(Env.CLIENT, () -> TwitchSpawn.Client::initializeClient);
		EnvExecutor.runInEnv(Env.SERVER, () -> TwitchSpawn.Server::initializeServer);
	}


    /**
     * List of common tasks initialized by both environments.
     */
	private static void initializeCommon()
	{
        // Trigger tracer on server start.
		LifecycleEvent.SERVER_BEFORE_START.register(server -> {
			SERVER = server;
			TRACE_MANAGER = new TraceManager();
		});

        // Trigger autostart if that is enabled.
		LifecycleEvent.SERVER_STARTING.register(server -> {
			if (ConfigManager.PREFERENCES.autoStart == PreferencesConfig.AutoStartEnum.ENABLED) {
				LOGGER.info("Auto-start is enabled. Attempting to start tracers.");
				TRACE_MANAGER.start();
			}
		});

        // Trigger server stop that would disable tracers.
		LifecycleEvent.SERVER_STOPPING.register(server -> {
			SERVER = null;

			if (TRACE_MANAGER.isRunning())
			{
				TRACE_MANAGER.stop(null, "Server stopping");
			}

			ConfigManager.RULESET_COLLECTION.clearQueue();
		});

        // Do stuff on player joining the server.
		PlayerEvent.PLAYER_JOIN.register(player ->
		{
			String translationKey = TRACE_MANAGER.isRunning() ?
				"commands.twitchspawn.status.on" : "commands.twitchspawn.status.off";

			player.sendSystemMessage(Component.translatable(translationKey));

			if (TRACE_MANAGER.isRunning())
			{
				TRACE_MANAGER.connectStreamer(player.getName().getString());
			}

			NetworkManager.CHANNEL.sendToPlayer(player, new StatusChangedPacket(TRACE_MANAGER.isRunning()));
		});

        // Do stuff on player leaving the server.
		PlayerEvent.PLAYER_QUIT.register(player ->
		{
			if (TRACE_MANAGER.isRunning())
			{
				TRACE_MANAGER.disconnectStreamer(player.getName().getString());
			}
		});

		try
		{
			TwitchSpawnSoundEvent.REGISTRY.register();
			TwitchSpawnArgumentTypes.registerArgumentType();

			ConfigManager.loadConfigs();
			NetworkManager.initialize();
		}
		catch (TwitchSpawnLoadingErrors e)
		{
			TwitchSpawnCommonEvent.SETUP_EVENT.invoker().setupEvent(e);
		}
	}


    /**
     * Client related tasks.
     */
	@Environment(EnvType.CLIENT)
	public static class Client
	{
		@Environment(EnvType.CLIENT)
		public static void initializeClient()
		{
			TwitchSpawn.initializeCommon();
			NotepadUDLUpdater.attemptUpdate();

			ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(player ->
			{
				StatusIndicatorOverlay.register();
				GlobalChatCooldownOverlay.register();
			});

            ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player ->
			{
				StatusIndicatorOverlay.unregister();
				GlobalChatCooldownOverlay.unregister();
			});
		}
	}


    /**
     * Server related tasks
     */
	@Environment(EnvType.SERVER)
	public static class Server
	{
		@Environment(EnvType.SERVER)
		public static void initializeServer()
		{
			TwitchSpawn.initializeCommon();
		}
	}
}
