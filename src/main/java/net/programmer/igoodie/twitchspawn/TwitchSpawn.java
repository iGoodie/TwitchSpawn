package net.programmer.igoodie.twitchspawn;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.programmer.igoodie.twitchspawn.client.gui.StatusIndicatorOverlay;
import net.programmer.igoodie.twitchspawn.command.TwitchSpawnCommand;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.network.NetworkManager;
import net.programmer.igoodie.twitchspawn.network.packet.StatusChangedPacket;
import net.programmer.igoodie.twitchspawn.tracer.TraceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        }

        try {
            ConfigManager.loadConfigs(event.getModConfigurationDirectory());
            NetworkManager.initialize();

//            ArgumentTypes.register("twitchspawn:streamer", StreamerArgumentType.class,
//                    new ArgumentSerializer<>(StreamerArgumentType::streamerNick));
//            ArgumentTypes.register("twitchspawn:ruleset", RulesetNameArgumentType.class,
//                    new ArgumentSerializer<>(RulesetNameArgumentType::rulesetName));

        } catch (TwitchSpawnLoadingErrors e) {
            e.display();
        }

        LOGGER.info("preInit()");
    }

    @SubscribeEvent
    public void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        // TODO: Find answer; Is this redundant? (See https://mcforge.readthedocs.io/en/latest/effects/sounds/)
        event.getRegistry().register(new SoundEvent(new ResourceLocation(TwitchSpawn.MOD_ID, "pop_in")));
        event.getRegistry().register(new SoundEvent(new ResourceLocation(TwitchSpawn.MOD_ID, "pop_out")));
        LOGGER.info("registerSounds()");
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
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayerMP entity = (EntityPlayerMP) event.player;

        String translationKey = TRACE_MANAGER.isRunning() ?
                "commands.twitchspawn.status.on" : "commands.twitchspawn.status.off";

        entity.sendMessage(new TextComponentTranslation(translationKey));

        NetworkManager.CHANNEL.sendTo(
                new StatusChangedPacket.Message(TRACE_MANAGER.isRunning()),
                entity);
        LOGGER.info("onPlayerLoggedIn()");
    }

}
