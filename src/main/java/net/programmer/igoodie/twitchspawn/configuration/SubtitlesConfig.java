package net.programmer.igoodie.twitchspawn.configuration;

import com.google.gson.JsonArray;

import java.io.File;

public class SubtitlesConfig extends TextComponentConfig {

    public static SubtitlesConfig create(File file) {
        return new SubtitlesConfig(file);
    }

    protected SubtitlesConfig(File file) {
        super(file);
    }

    @Override
    protected String defaultResourcePath() {
        return "assets/twitchspawn/default/subtitles.default.json";
    }

    @Override
    public JsonArray getTextComponent(String name) {
        return super.getTextComponent(name.toUpperCase());
    }

}
