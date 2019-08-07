package net.programmer.igoodie.twitchspawn.configuration;

import com.google.common.io.Resources;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.programmer.igoodie.twitchspawn.util.GsonUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

// TODO implement an abstraction for this and TitleConfig
public class SubtitlesConfig {

    public static SubtitlesConfig create(File file) {
        try {
            JsonObject defaultJson = defaultJson();
            JsonObject json;

            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
                json = new JsonParser().parse("{}").getAsJsonObject();
            } else {
                String jsonRaw = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                json = new JsonParser().parse(jsonRaw).getAsJsonObject();
            }

            GsonUtils.copyNonExistingFields(defaultJson, json);

            GsonUtils.forEachField(json, field -> {
                // Replace non-array fields with an empty array
                if (!json.get(field).isJsonArray())
                    json.add(field, defaultJson.get(field));

                GsonUtils.removeInvalidTextComponent(json.getAsJsonArray(field));
            });

            FileUtils.writeStringToFile(file, GsonUtils.prettyJson(json), StandardCharsets.UTF_8);

            return new SubtitlesConfig(json);

        } catch (IOException e) {
            throw new InternalError("Tried to read from or save to a non-existing file");
        } catch (IllegalStateException e) {
            throw new JsonSyntaxException("Expected a JSON object.");
        }
    }

    private static JsonObject defaultJson() {
        try {
            URL location = Resources.getResource("assets/twitchspawn/default/subtitles.default.json");
            String jsonRaw = Resources.toString(location, StandardCharsets.UTF_8);
            return new JsonParser().parse(jsonRaw).getAsJsonObject();

        } catch (IOException e) {
            throw new InternalError("Missing default file: ../assets/twitchspawn/default/subtitles.default.json");
        } catch (JsonSyntaxException e) {
            throw new InternalError("Malformed default file: ../assets/twitchspawn/default/subtitles.default.json");
        }
    }

    /* ---------------------------------------- */

    private JsonObject subtitles;

    private SubtitlesConfig(JsonObject titles) {
        this.subtitles = titles;
    }

    public String getSubtitleJsonRaw(String actionName) {
        JsonArray array = subtitles.getAsJsonArray(actionName);
        return array == null ? "[]" : array.toString();
    }

}
