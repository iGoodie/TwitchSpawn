package igoodie.twitchspawn.configs;

import java.io.File;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import igoodie.twitchspawn.utils.FileUtils;

//TODO: Capability for server initialization
public class ConfigsValidator {
	/**
	 * Existance test of configs
	 */
	public static boolean preValidate() {
		//Flag it's valid initially.
		boolean valid = true;
		
		//Check if config direction for TwitchSpawn is there. Create if not there.
		if(!FileUtils.fileExists(Configs.CONFIG_DIR)) {
			FileUtils.createDir(Configs.CONFIG_DIR);
			valid = false;
		}
		
		//Check if config.json in config direction exists. Create if not there.
		if(!FileUtils.fileExists(Configs.CONFIG_DIR + File.separatorChar + "config.json")) {
			String jsonRead = FileUtils.readPrototypeString("configs/config.json");
			Configs.json = new JsonParser().parse(jsonRead).getAsJsonObject();
			valid = false;
		}
		
		//TODO: Check all config files in the direction using prototypes
		
		//If not valid, then config files should be changed before. Save if not valid.
		if(!valid) Configs.save();
		return valid;
	}
	
	/**
	 * Validation test of configs
	 */
	public static boolean validate() {
		//Flag it's valid initially.
		boolean valid = true;
		
		//TODO: Delete non-valid lines for every config file
		//TODO: Fix read prototype for server capability.
		
		String jsonRead = "{\"access_token\":\"!Your Legacy Streamlabs API Token here!\",\"streamer_mc_nick\":\"!Your Minecraft nick here!\",\"streamer_twitch_nick\":\"!Your Twitch channel name here!\",\"rewards\":[{\"minimum_currency\":0,\"blocks\":[\"minecraft:stick\",\"minecraft:apple\"]},{\"minimum_currency\":1,\"blocks\":[\"minecraft:diamond_block\"]}]}";
		JsonObject defaultConfigs = new JsonParser().parse(jsonRead).getAsJsonObject();
		for(Entry<String, JsonElement> j : defaultConfigs.entrySet()) {
			if(!Configs.json.has(j.getKey())) {
				Configs.json.add(j.getKey(), j.getValue());
				valid = false;
			}
		}
		
		//TODO: Check all config files in the directory using prototypes
		
		//If not valid, then config files should be changed before. Save if not valid.
		if(!valid) Configs.save();
		return valid;
	}
}
