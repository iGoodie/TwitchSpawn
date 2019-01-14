package igoodie.twitchspawn.configs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ConfigValidator {
	
	public static JsonObject validateCustomText(JsonObject customText) {
		JsonObject temp = new JsonObject();		
		
		copy(customText, temp, "donation", parseJson("{\"upper_text_format\":\"${actor} donated ${amount} USD to ${streamer}!\",\"lower_text_format\":\"${actor} rewarded you with ${item}!\"}"));
		copy(customText, temp, "bit_donation", parseJson("{\"upper_text_format\":\"${actor} donated ${amount_i} bit(s)!\",\"lower_text_format\":\"${actor} rewarded you with ${item}\"}"));
		copy(customText, temp, "subscription", parseJson("{\"upper_text_format\":\"${actor} is a subscriber for ${amount_i} month(s)!\",\"lower_text_format\":\"${actor} rewarded you with ${item}\"}"));
		copy(customText, temp, "follow", parseJson("{\"upper_text_format\":\"${actor} followed you!\",\"lower_text_format\":\"${actor} rewarded you with ${item}\"}"));
		copy(customText, temp, "host", parseJson("{\"upper_text_format\":\"${actor} hosted you to ${amount_i} viewer(s)!\",\"lower_text_format\":\"${actor} rewarded you with ${item}\"}"));
		
		return temp;
	}

	public static JsonObject validateGeneralConfig(JsonObject configJson) {
		JsonObject temp = new JsonObject();

		copy(configJson, temp, "access_token", "!Your Streamlabs API Access Token here!");
		copy(configJson, temp, "socket_api_token", "!Your Streamlabs Socket API Token here!");
		copy(configJson, temp, "streamer_mc_nick", "!Your Minecraft nick here!");
		copy(configJson, temp, "streamer_twitch_nick", "!Your Twitch channel name here!");

		copy(configJson, temp, "moderator_mc_nicks", new JsonArray());
		
		copy(configJson, temp, "rewards", parseJson("{\"follow_rewards\":[\"minecraft:stone\",\"minecraft:diamond_block\"],\"host_rewards\":[{\"minimum_viewer\":0,\"items\":[\"minecraft:stick\"]},{\"minimum_viewer\":10,\"items\":[\"minecraft:diamond\"]}],\"bit_rewards\":[{\"minimum_bit\":0,\"items\":[\"minecraft:stick\",\"minecraft:apple\"]},{\"minimum_bit\":100,\"items\":[\"minecraft:diamond_block\"]}],\"donation_rewards\":[{\"minimum_amount\":0,\"items\":[\"minecraft:stick\",\"minecraft:apple\"]}],\"sub_rewards\":[{\"minimum_months\":0,\"items\":[\"minecraft:stick\",\"minecraft:apple\"]},{\"minimum_months\":10,\"items\":[\"minecraft:diamond_block\"]}]}"));
		
		copySubArray(configJson, temp, "rewards", "follow_rewards", parseJsonArray("[\"minecraft:stone\",\"minecraft:diamond_block\"]"));
		copySubArray(configJson, temp, "rewards", "host_rewards", parseJsonArray("[{\"minimum_viewer\":0,\"items\":[\"minecraft:stick\"]},{\"minimum_viewer\":10,\"items\":[\"minecraft:diamond\"]}]"));
		copySubArray(configJson, temp, "rewards", "bit_rewards", parseJsonArray("[{\"minimum_bit\":0,\"items\":[\"minecraft:stick\",\"minecraft:apple\"]},{\"minimum_bit\":100,\"items\":[\"minecraft:diamond_block\"]}]"));
		copySubArray(configJson, temp, "rewards", "donation_rewards", parseJsonArray("[{\"minimum_amount\":0,\"items\":[\"minecraft:stick\",\"minecraft:apple\"]}]"));
		copySubArray(configJson, temp, "rewards", "sub_rewards", parseJsonArray("[{\"minimum_months\":0,\"items\":[\"minecraft:stick\",\"minecraft:apple\"]},{\"minimum_months\":10,\"items\":[\"minecraft:diamond_block\"]}]"));
		
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
	
	public static void copySubArray(JsonObject from, JsonObject to, String fieldName, String arrayName, JsonArray defaultValue) {
		JsonObject field = from.has(fieldName) ? from.get(fieldName).getAsJsonObject() : new JsonObject();
		
		if(!field.has(arrayName)) { // Copy default value, if array doesn't exist under field
			to.get(fieldName).getAsJsonObject().add(arrayName, defaultValue);
		} else { // Copy if exists tho
			to.get(fieldName).getAsJsonObject().add(arrayName, field.get(arrayName).getAsJsonArray());
		}
	}

	private static JsonObject parseJson(String raw) {
		return new JsonParser().parse(raw).getAsJsonObject();
	}
	
	private static JsonArray parseJsonArray(String raw) {
		return new JsonParser().parse(raw).getAsJsonArray();
	}
	
}
