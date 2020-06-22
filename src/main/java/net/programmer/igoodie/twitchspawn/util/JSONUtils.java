package net.programmer.igoodie.twitchspawn.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.function.Consumer;

public class JSONUtils {

    public static Number extractNumberFrom(JSONObject json, String key, Number defaultValue) {
        Object value = null;

        try {
            value = json.get(key);

            if (value instanceof String)
                return Double.parseDouble((String) value);

            return (Number) value;

        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static <T> T extractFrom(JSONObject json, String key, Class<T> type, T defaultValue) {
        try {
            Object value = json.get(key);
            return type.cast(value);

        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static void forEach(JSONArray array, Consumer<JSONObject> consumer) {
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject json = array.getJSONObject(i);
                consumer.accept(json);

            } catch (JSONException e) {
                throw new InternalError("Error performing JSONArray forEachMessage.");
            }
        }
    }

    public static String escape(String jsonString) {
        StringBuilder escapedString = new StringBuilder();

        for (char character : jsonString.toCharArray()) {
            if (character == '\'' || character == '"' || character == '\\') {
                escapedString.append("\\");
            }

            escapedString.append(character);
        }

        return escapedString.toString();
    }

}
