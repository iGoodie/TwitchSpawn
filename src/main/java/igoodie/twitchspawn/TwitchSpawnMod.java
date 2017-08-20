package igoodie.twitchspawn;

import igoodie.twitchspawn.command.CommandTwitchSpawn;
import igoodie.twitchspawn.configs.Configs;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid=TSConstants.MOD_ID, version=TSConstants.MOD_VERSION)
public class TwitchSpawnMod implements TSConstants {
	@EventHandler
	public void preinit(FMLPreInitializationEvent event) {
		Configs.init(event.getSuggestedConfigurationFile());
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
	}
	
	@EventHandler
	public void postinit(FMLPostInitializationEvent event) {
	}
	
	@EventHandler
	public void preInitServer(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandTwitchSpawn());
	}
}
