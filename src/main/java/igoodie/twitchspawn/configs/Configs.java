package igoodie.twitchspawn.configs;

import igoodie.twitchspawn.utils.FileUtils;

import java.io.File;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Configs {
	public static JsonObject json;

	private static String cfgDir;
	
	public static void init(File file) {
		//Save direction of TwitchSpawn configs
		cfgDir = file.getParent() + File.separatorChar + "TwitchSpawn";
		
		//Create parser and create env if absent
		if(!FileUtils.fileExists(cfgDir)) {
			json = new JsonParser().parse("{\"access_token\":\"!Your legacy api token here!\",\"streamer_nick\":\"!Your minecraft nick here!\",\"rewards\":[{\"minimum_currency\":0,\"blocks\":[\"minecraft:stick\",\"minecraft:apple\"]},{\"minimum_currency\":1,\"blocks\":[]}]}").getAsJsonObject();
			FileUtils.createDir(cfgDir);
			save();
		}
		
		//Load from configs
		load();
	}
	
	public static void save() {
		FileUtils.writeString(beautifyJson(json), cfgDir+File.separatorChar+"config.json");
	}
	
	public static void load() {
		json = new JsonParser().parse(FileUtils.readString(cfgDir+File.separatorChar+"config.json")).getAsJsonObject();
	}

	private static String beautifyJson(JsonElement json) {
		return new GsonBuilder().setPrettyPrinting().create().toJson(json);
	}
}
