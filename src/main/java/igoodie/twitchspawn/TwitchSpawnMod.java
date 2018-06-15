package igoodie.twitchspawn;

import org.apache.logging.log4j.Logger;

import igoodie.twitchspawn.command.CommandTwitchSpawn;
import igoodie.twitchspawn.configs.Configs;
import igoodie.twitchspawn.network.Packets;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid=TSConstants.MOD_ID, version=TSConstants.MOD_VERSION)
public class TwitchSpawnMod implements TSConstants {
	
	public static Logger logger;
	
	@EventHandler
	public void preinit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		Configs.init(event.getSuggestedConfigurationFile());
		Packets.init();
		logger.info("Done pre-init for physical side: " + event.getSide());
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		logger.info("Done init for physical side: " + event.getSide());
	}
	
	@EventHandler
	public void postinit(FMLPostInitializationEvent event) {
		logger.info("Done post-init for physical side: " + event.getSide());
	}
	
	@EventHandler
	public void preInitServer(FMLServerStartingEvent event) {
		Packets.init();
		event.registerServerCommand(new CommandTwitchSpawn());
	}
}
