package net.programmer.igoodie.twitchspawn.configuration;

import com.google.gson.JsonArray;

import java.io.File;

public class TitlesConfig extends TextComponentConfig {

    public static TitlesConfig create(File file) {
        return new TitlesConfig(file);
    }

    protected TitlesConfig(File file) {
        super(file);
    }

    @Override
    protected String defaultResourcePath() {
        return "assets/twitchspawn/default/titles.default.json";
    }

    @Override
    public JsonArray getTextComponent(String name) {
        return super.getTextComponent(name.toLowerCase());
    }

}
