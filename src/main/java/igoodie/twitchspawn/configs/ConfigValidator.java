package igoodie.twitchspawn.configs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ConfigValidator {

	public static JsonObject validate(JsonObject configJson) {
		JsonObject temp = new JsonObject();

		copy(configJson, temp, "access_token", "!Your Streamlabs API Access Token here!");
		copy(configJson, temp, "socket_api_token", "!Your Streamlabs Socket API Token here!");
		copy(configJson, temp, "streamer_mc_nick", "!Your Minecraft nick here!");
		copy(configJson, temp, "streamer_twitch_nick", "!Your Twitch channel name here!");

		copy(configJson, temp, "moderator_mc_nicks", new JsonArray());
		copy(configJson, temp, "rewards", parseJson("{\"bit_rewards\":[{\"minimum_bit\":0,\"items\":[\"minecraft:stick\",\"minecraft:apple\"]},{\"minimum_bit\":100,\"items\":[\"minecraft:diamond_block\"]}],\"donation_rewards\":[{\"minimum_amount\":0,\"items\":[\"minecraft:stick\",\"minecraft:apple\"]}],\"sub_rewards\":[{\"minimum_months\":0,\"items\":[\"minecraft:stick\",\"minecraft:apple\"]},{\"minimum_months\":10,\"items\":[\"minecraft:diamond_block\"]}]}"));

		// TODO: In-depth validation for rewards entities
		
		return temp;
	}

	public static void copy(JsonObject from, JsonObject to, String fieldName, String defaultValue) {
		if (!from.has(fieldName)) { // Copy default value, if fieldName is absent
			to.addProperty(fieldName, defaultValue);
			return;
		}

		try { // Try to fetch a string and put. If type is invalid, put default value
			String value = from.get(fieldName).getAsString();
			to.addProperty(fieldName, value);
		} catch (ClassCastException e) {
			to.addProperty(fieldName, defaultValue);
		}
	}
	
	public static void copy(JsonObject from, JsonObject to, String fieldName, JsonArray defaultValue) {
		if (!from.has(fieldName)) { // Copy default value, if fieldName is absent
			to.add(fieldName, defaultValue);
			return;
		}
		
		try { // Try to fetch an array and put. If type is invalid, put default value
			JsonArray value = from.get(fieldName).getAsJsonArray();
			to.add(fieldName, value);
		} catch (ClassCastException e) {
			to.add(fieldName, defaultValue);
		}
	}
	
	public static void copy(JsonObject from, JsonObject to, String fieldName, JsonObject defaultValue) {
		if (!from.has(fieldName)) { // Copy default value, if fieldName is absent
			to.add(fieldName, defaultValue);
			return;
		}
		
		try { // Try to fetch an object and put. If type is invalid, put default value
			JsonObject value = from.get(fieldName).getAsJsonObject();
			to.add(fieldName, value);
		} catch (ClassCastException e) {
			to.add(fieldName, defaultValue);
		}
	}

	private static JsonObject parseJson(String raw) {
		return new JsonParser().parse(raw).getAsJsonObject();
	}
	
}
