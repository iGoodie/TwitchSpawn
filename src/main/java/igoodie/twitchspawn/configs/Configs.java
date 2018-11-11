package igoodie.twitchspawn.configs;

import java.io.File;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import igoodie.twitchspawn.TwitchSpawn;
import igoodie.twitchspawn.utils.FileUtils;

public class Configs {
	public static JsonObject configJson;

	public static String CONFIGS_DIR;
	public static String CONFIG_PATH;

	public static void init(File configDirection) {
		CONFIGS_DIR = configDirection.getParent() + File.separatorChar + "TwitchSpawn";
		CONFIG_PATH = CONFIGS_DIR + File.separatorChar + "config.json";
		
		// If file exists, try to load. Exit if syntax error exists.
		if(FileUtils.fileExists(CONFIG_PATH)) {
			try { load(); } catch(JsonParseException e) {
				TwitchSpawn.LOGGER.error("Invalid JSON syntax in ../config/TwitchSpawn/config.json");
				System.exit(0); // Force exit. TODO: Find a better solution
			}			
		} else { // Load default configs otherwise
			load("{}");
			TwitchSpawn.LOGGER.warn("TwitchSpawn config loaded as {}. Created/wrote default configs.");
		}
	}

	public static void save() {
		FileUtils.writeString(beautifyJson(configJson), CONFIG_PATH);
	}

	public static void load() {
		load(FileUtils.readString(CONFIG_PATH));
	}
	
	public static void load(String jsonString) {
		configJson = new JsonParser().parse(jsonString).getAsJsonObject();
		configJson = ConfigValidator.validate(configJson); // Validate on load and save
		save();
	}

	public static String beautifyJson(JsonElement json) {
		return new GsonBuilder().setPrettyPrinting().create().toJson(json);
	}
}
