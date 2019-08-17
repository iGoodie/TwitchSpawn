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

public abstract class TextComponentConfig {

    protected JsonObject componentJson;

    protected TextComponentConfig(File file) {
        this.componentJson = readComponents(file, readDefaults());
    }

    protected JsonObject readComponents(File from, JsonObject defaultComponents) throws JsonSyntaxException {
        try {
            JsonObject components;
            String jsonRaw;

            if (!from.exists()) {
                from.getParentFile().mkdirs();
                from.createNewFile();
                jsonRaw = "{}";

            } else {
                jsonRaw = FileUtils.readFileToString(from, StandardCharsets.UTF_8);
            }

            components = new JsonParser().parse(jsonRaw).getAsJsonObject();

            GsonUtils.copyNonExistingFields(defaultComponents, components);

            GsonUtils.forEachField(components, field -> {
                if (!components.get(field).isJsonArray()) {
                    components.add(field, defaultComponents.get(field));
                }

                JsonArray component = components.getAsJsonArray(field);

                GsonUtils.removeInvalidTextComponent(component);
            });

            FileUtils.writeStringToFile(from, GsonUtils.prettyJson(components), StandardCharsets.UTF_8);

            return components;

        } catch (IOException e) {
            throw new InternalError("Tried to read from or save to a non-existing file");

        } catch (IllegalStateException e) {
            throw new JsonSyntaxException("Expected a JSON object.");

        } catch (ClassCastException e) {
            throw new JsonSyntaxException("Expected each component to be a JSON array.");
        }
    }

    private JsonObject readDefaults() {
        try {
            URL location = Resources.getResource(defaultResourcePath());
            String jsonRaw = Resources.toString(location, StandardCharsets.UTF_8);
            return new JsonParser().parse(jsonRaw).getAsJsonObject();

        } catch (IOException e) {
            throw new InternalError("Missing default file: ../" + defaultResourcePath());

        } catch (JsonSyntaxException e) {
            throw new InternalError("Malformed default file: ../" + defaultResourcePath());
        }
    }

    protected abstract String defaultResourcePath();

    /* ----------------------------------------- */

    public JsonArray getTextComponent(String name) {
        return componentJson.getAsJsonArray(name);
    }

    public String getTextComponentRaw(String name) {
        JsonArray textComponent = getTextComponent(name);
        return textComponent == null ? "[]" : textComponent.toString();
    }

}
