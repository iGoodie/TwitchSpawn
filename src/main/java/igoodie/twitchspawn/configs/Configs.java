package igoodie.twitchspawn.configs;

import igoodie.twitchspawn.TwitchSpawn;
import igoodie.twitchspawn.utils.FileUtils;
import net.minecraft.util.StringUtils;

import java.io.File;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Configs {
	public static JsonObject json;

	public static String CONFIG_DIR;

	public static void init(File file) {
		//Save direction of TwitchSpawn configs
		CONFIG_DIR = file.getParent() + File.separatorChar + "TwitchSpawn";

		load();
		
		if(validate())
			save();
		
		// TODO: Remove invalid keys
	}

	private static boolean validate() {
		boolean changesMade = false;
		
		// Create config dir if not present
		if(!FileUtils.fileExists(CONFIG_DIR)) {
			FileUtils.createDir(CONFIG_DIR);
			changesMade = true;
		}
		
		// Add tokens
		changesMade |= addValue("access_token", "!Your Streamlabs API Access Token here!");
		changesMade |= addValue("socket_api_token", "!Your Streamlabs Socket API Token here!");
		
		// Add nicks
		changesMade |= addValue("streamer_mc_nick", "!Your Minecraft nick here!");
		changesMade |= addValue("streamer_twitch_nick", "!Your Twitch channel name here!");
		
		// Add example selectors
		String selectors = "{\"bit_rewards\":[{\"minimum_bit\":0,\"items\":[\"minecraft:stick\",\"minecraft:apple\"]},{\"minimum_bit\":100,\"items\":[\"minecraft:diamond_block\"]}],\"donation_rewards\":[{\"minimum_amount\":0,\"items\":[\"minecraft:stick\",\"minecraft:apple\"]}],\"sub_rewards\":[{\"minimum_months\":0,\"items\":[\"minecraft:stick\",\"minecraft:apple\"]},{\"minimum_months\":10,\"items\":[\"minecraft:diamond_block\"]}]}";
		changesMade |= addValue("rewards", new JsonParser().parse(selectors).getAsJsonObject());
		
		return changesMade;
	}
	
	private static boolean addValue(String key, JsonElement value) {
		if(json.has(key)) return false; // Changes not made
		
		json.add(key, value);
		return true; // Changes made
	}
	
	private static boolean addValue(String key, String value) {
		if(json.has(key)) return false; // Changes not made
		
		json.addProperty(key, value);
		return true; // Changes made
	}

	public static void save() {
		FileUtils.writeString(beautifyJson(json), CONFIG_DIR+File.separatorChar+"config.json");
	}

	public static void load() {
		String jsonStr = FileUtils.readString(CONFIG_DIR+File.separatorChar+"config.json");
		
		if(StringUtils.isNullOrEmpty(jsonStr)) {
			jsonStr = "{}";
			TwitchSpawn.LOGGER.warn("TwitchSpawn config loaded as {}. Something suspicious happens hier..");
		}
		
		json = new JsonParser().parse(jsonStr).getAsJsonObject();
	}

	public static String beautifyJson(JsonElement json) {
		return new GsonBuilder().setPrettyPrinting().create().toJson(json);
	}
}
