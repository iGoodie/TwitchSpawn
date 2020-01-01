package net.programmer.igoodie.twitchspawn.configuration.validation;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

public class JSONValidator {

    private JsonObject actualJson;
    private JsonObject expectedJson;
    private JsonObject builtJson;

    public JSONValidator(String actualScript, String defaultScript) {
        Gson gson = new Gson();
        this.actualJson = gson.fromJson(actualScript, JsonObject.class);
        this.expectedJson = gson.fromJson(actualScript, JsonObject.class);
        this.builtJson = new JsonObject();
    }

    public boolean isValid() {
        return false; // TODO
    }

    public JsonObject validate() {


        return builtJson;
    }

    private JsonObject validate(JsonObject actualJson, JsonObject expectedJson) {
        JsonObject validated = new JsonObject();

        // Traverse each key-value pair in Expected JSON
        for (Map.Entry<String, JsonElement> property : expectedJson.entrySet()) {
            String key = property.getKey();
            JsonElement expectedValue = property.getValue();

            // Value in the Expected JSON is a primitive
//            if (expectedValue.isJsonPrimitive()) {
//                validated.add(key, actualJson.has(key) && actualJson.get(key).getClass()
//                        ? actualJson.get(key)
//                        : expectedValue);
//            }

            // Value in the Expected JSON is an array
            if(expectedValue.isJsonArray()) {

            }
        }

        return validated;
    }

}
