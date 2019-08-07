package net.programmer.igoodie.twitchspawn.configuration;

import com.google.common.io.Resources;
import com.google.gson.*;
import net.programmer.igoodie.twitchspawn.util.GsonUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TitlesConfig {

    public static TitlesConfig create(File file) throws JsonSyntaxException {
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

            return new TitlesConfig(json);

        } catch (IOException e) {
            throw new InternalError("Tried to read from or save to a non-existing file");
        } catch (IllegalStateException e) {
            throw new JsonSyntaxException("Expected a JSON object.");
        }
    }

    private static JsonObject defaultJson() {
        try {
            URL location = Resources.getResource("assets/twitchspawn/default/titles.default.json");
            String jsonRaw = Resources.toString(location, StandardCharsets.UTF_8);
            return new JsonParser().parse(jsonRaw).getAsJsonObject();

        } catch (IOException e) {
            throw new InternalError("Missing default file: ../assets/twitchspawn/default/titles.default.json");
        } catch (JsonSyntaxException e) {
            throw new InternalError("Malformed default file: ../assets/twitchspawn/default/titles.default.json");
        }
    }

    /* ---------------------------------------- */

    private JsonObject titles;

    private TitlesConfig(JsonObject titles) {
        this.titles = titles;
    }

    public String getTitleJsonRaw(String eventAlias) {
        return titles.getAsJsonArray(eventAlias.toLowerCase()).toString();
    }

}
