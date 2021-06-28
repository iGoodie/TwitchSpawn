package net.programmer.igoodie.twitchspawn.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

// Wutax' beauty! XD
public abstract class JSONConfig {

    private static Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
    protected String extension = ".json";

    public void generateConfig() {
        this.fillEmpty();

        try {
            this.writeConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getConfigFile() {
        return new File(ConfigManager.CONFIG_DIR_PATH + File.separator
                + this.getName() + this.extension);
    }

    public abstract String getName();

    public JSONConfig readConfig() {
        try {
            JSONConfig config = GSON.fromJson(new FileReader(this.getConfigFile()), this.getClass());
            config.fillEmpty();
            config.writeConfig();
            return config;
        } catch (IOException e) {
            this.generateConfig();
        }

        return this;
    }

    protected abstract void fillEmpty();

    public void writeConfig() throws IOException {
        File dir = new File(ConfigManager.CONFIG_DIR_PATH);
        if (!dir.exists() && !dir.mkdirs()) return;
        if (!this.getConfigFile().exists() && !this.getConfigFile().createNewFile()) return;
        FileWriter writer = new FileWriter(this.getConfigFile());
        GSON.toJson(this, writer);
        writer.flush();
        writer.close();
    }

}
