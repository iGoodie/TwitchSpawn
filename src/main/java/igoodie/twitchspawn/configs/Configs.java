package igoodie.twitchspawn.configs;

import java.io.File;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import igoodie.twitchspawn.TSConstants;
import igoodie.twitchspawn.TwitchSpawn;
import igoodie.twitchspawn.utils.FileUtils;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.server.FMLServerHandler;

// TODO: Do moar abstraction, it's getting terrible :c
public class Configs {
	public static JsonObject configJson;
	public static JsonObject customText;

	public static String CONFIGS_DIR;
	public static String CONFIG_PATH;
	public static String CUSTOM_TEXT_PATH;

	public static void init(File configDirection) {
		CONFIGS_DIR = configDirection.getParent() + File.separatorChar + "TwitchSpawn";
		CONFIG_PATH = CONFIGS_DIR + File.separatorChar + "config.json";
		CUSTOM_TEXT_PATH = CONFIGS_DIR + File.separatorChar + "custom_text.json";
		
		// If file exists, try to load. Exit if syntax error exists.
		if(FileUtils.fileExists(CONFIG_PATH)) {
			try { loadGeneralConfig(); } catch(JsonParseException e) {
				TwitchSpawn.LOGGER.error("Invalid JSON syntax in ../config/TwitchSpawn/config.json");
				FMLCommonHandler.instance().exitJava(TSConstants.EXIT_INVALID_CONFIG, false);
			}			
		} else { // Load default configs otherwise
			loadGeneralConfig("{}");
			TwitchSpawn.LOGGER.warn("TwitchSpawn config.json loaded as {}. Created/wrote default configs.");
		}
		
		// If file exists, try to load. Exit if syntax error exists.
		if(FileUtils.fileExists(CUSTOM_TEXT_PATH)) {
			try { loadCustomText(); } catch(JsonParseException e) {
				TwitchSpawn.LOGGER.error("Invalid JSON syntax in ../config/TwitchSpawn/custom_text.json");
				FMLCommonHandler.instance().exitJava(TSConstants.EXIT_INVALID_CONFIG, false);
			}			
		} else { // Load default configs otherwise
			loadCustomText("{}");
			TwitchSpawn.LOGGER.warn("TwitchSpawn custom_text.json loaded as {}. Created/wrote default configs.");
		}
	}

	/* config.json */
	public static void saveGeneralConfig() {
		FileUtils.writeString(beautifyJson(configJson), CONFIG_PATH);
	}

	public static void loadGeneralConfig() {
		loadGeneralConfig(FileUtils.readString(CONFIG_PATH));
	}
	
	public static void loadGeneralConfig(String jsonString) {
		configJson = new JsonParser().parse(jsonString).getAsJsonObject();
		configJson = ConfigValidator.validateGeneralConfig(configJson); // Validate on load and save
		saveGeneralConfig();
	}
	
	/* custom_text.json */
	public static void saveCustomText() {
		FileUtils.writeString(beautifyJson(customText), CUSTOM_TEXT_PATH);
	}
	
	public static void loadCustomText() {
		loadCustomText(FileUtils.readString(CUSTOM_TEXT_PATH));
	}
	
	public static void loadCustomText(String jsonString) {
		customText = new JsonParser().parse(jsonString).getAsJsonObject();
		customText = ConfigValidator.validateCustomText(customText); // Validate on load and save
		saveCustomText();
	}

	/* Helper(s) */
	private static String beautifyJson(JsonElement json) {
		return new GsonBuilder().setPrettyPrinting().create().toJson(json);
	}
}
