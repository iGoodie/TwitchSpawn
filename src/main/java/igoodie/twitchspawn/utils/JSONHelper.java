package igoodie.twitchspawn.utils;

import java.util.function.Consumer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonArray;

public class JSONHelper {

	public static String extractString(JSONObject json, String key) {
		return extractString(json, key, null);
	}

	public static String extractString(JSONObject json, String key, String defaultValue) {
		try {
			return json.getString(key);
		} catch(JSONException e) {
			return defaultValue;
		}
	}

	public static double extractDouble(JSONObject json, String key) {
		return extractDouble(json, key, 0.0);
	}

	public static double extractDouble(JSONObject json, String key, double defaultValue) {
		try {
			return json.getDouble(key);
		} catch(JSONException e) {
			return defaultValue;
		}
	}

	public static JSONArray extractJSONArray(JSONObject json, String key) {
		try {
			return json.getJSONArray(key);
		} catch(JSONException e) {
			return null;
		}
	}

	public static void forEachJSONObject(JSONArray jsonArray, Consumer<JSONObject> consumer) {
		for(int i=0; i<jsonArray.length(); i++) {
			try {
				JSONObject obj = jsonArray.getJSONObject(i);
				consumer.accept(obj);
			} catch (JSONException e) {
				throw new InternalError("Internal error while performing JSONArray forEach");
			}
		}
	}

	public static boolean jsonArrayContains(JsonArray jsonArray, String value) {
		for(int i=0; i<jsonArray.size(); i++) {
			if(jsonArray.get(i).getAsString().equals(value))
				return true;
		}

		return false;
	}
}
