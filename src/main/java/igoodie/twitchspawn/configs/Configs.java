package igoodie.twitchspawn.configs;

import igoodie.twitchspawn.utils.FileUtils;

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
		
		//Pre-validate
		ConfigsValidator.preValidate();
		
		//Load from configs
		load();
		
		//Validate
		ConfigsValidator.validate();
	}
	
	public static void save() {
		FileUtils.writeString(beautifyJson(json), CONFIG_DIR+File.separatorChar+"config.json");
	}
	
	public static void load() {
		String jsonStr = FileUtils.readString(CONFIG_DIR+File.separatorChar+"config.json");
		jsonStr = jsonStr==null||jsonStr.isEmpty() ? "{}" : jsonStr;
		json = new JsonParser().parse(jsonStr).getAsJsonObject();
	}

	public static String beautifyJson(JsonElement json) {
		return new GsonBuilder().setPrettyPrinting().create().toJson(json);
	}
}
