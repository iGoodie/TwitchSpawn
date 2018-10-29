package igoodie.twitchspawn;

import org.apache.logging.log4j.Logger;

import igoodie.twitchspawn.command.CommandTwitchSpawn;
import igoodie.twitchspawn.configs.Configs;
import igoodie.twitchspawn.packet.TwitchSpawnPacketHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid=TSConstants.MOD_ID, version=TSConstants.MOD_VERSION)
public class TwitchSpawn implements TSConstants {
	
	public static Logger LOGGER;
	
	@EventHandler
	public void preinit(FMLPreInitializationEvent event) {
		LOGGER = event.getModLog();
		
		Configs.init(event.getSuggestedConfigurationFile());
		TwitchSpawnPacketHandler.init();
		
		LOGGER.info("Done pre-init for physical side: " + event.getSide());
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		LOGGER.info("Done init for physical side: " + event.getSide());
	}
	
	@EventHandler
	public void postinit(FMLPostInitializationEvent event) {
		LOGGER.info("Done post-init for physical side: " + event.getSide());
	}
	
	@EventHandler
	public void preInitServer(FMLServerStartingEvent event) {
		TwitchSpawnPacketHandler.init();
		event.registerServerCommand(new CommandTwitchSpawn());
	}
}
