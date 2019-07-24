package net.programmer.igoodie.twitchspawn.util;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Consumer;

public class GsonUtils {

    public static String prettyJson(JsonElement json) {
        return new GsonBuilder().setPrettyPrinting().create().toJson(json);
    }

    public static void copyNonExistingFields(JsonObject from, JsonObject to) {
        from.entrySet().forEach(entry -> {
            String field = entry.getKey();
            JsonElement value = entry.getValue();

            if (!to.has(field))
                to.add(field, value);
        });
    }

    public static void removeInvalidTextComponent(JsonArray array) {
        for (int i = array.size() - 1; i >= 0; i--) {
            JsonElement element = array.get(i);

            // Primitives are valid text components
            if(element.isJsonPrimitive())
                continue;

            // Json objects with primitive "text" field are valid text components
            if(element.isJsonObject() && element.getAsJsonObject().has("text")
                    && element.getAsJsonObject().get("text").isJsonPrimitive())
                continue;

            // Recursive text component
            if(element.isJsonArray()) {
                removeInvalidTextComponent(element.getAsJsonArray());
                continue;
            }

            array.remove(i);
        }
    }

    public static void forEachField(JsonObject json, Consumer<String> consumer) {
        json.entrySet().forEach(entry -> {
            consumer.accept(entry.getKey());
        });
    }

}
