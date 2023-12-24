package net.programmer.igoodie.twitchspawn;

import com.electronwill.nightconfig.core.io.ParsingException;
import com.google.gson.JsonSyntaxException;
import net.minecraftforge.fml.*;
import net.minecraftforge.forgespi.language.IModInfo;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxErrors;

import java.util.LinkedList;
import java.util.List;

public class TwitchSpawnLoadingErrors extends Exception {

    List<Exception> exceptions;

    public TwitchSpawnLoadingErrors() {
        this.exceptions = new LinkedList<>();
    }

    public void addException(Exception exception) {
        if (exception instanceof TSLSyntaxErrors)
            this.exceptions.addAll(((TSLSyntaxErrors) exception).getErrors());
        else
            this.exceptions.add(exception);
    }

    public boolean isEmpty() {
        return exceptions.size() == 0;
    }

    public void bindFMLWarnings(ModLoadingStage stage) {
        ModContainer modContainer = ModList.get()
                .getModContainerById(TwitchSpawn.MOD_ID).get();

        IModInfo modInfo = modContainer.getModInfo();

        for (Exception exception : exceptions) {
            String i18nMessage;

            if (exception instanceof TSLSyntaxError)
                i18nMessage = "modloader.twitchspawn.error.tsl";
            else if (exception instanceof ParsingException)
                i18nMessage = "modloader.twitchspawn.error.toml";
            else if (exception instanceof JsonSyntaxException)
                i18nMessage = "modloader.twitchspawn.error.json";
            else
                i18nMessage = "modloader.twitchspawn.error.unknown";

            ModLoadingWarning warning = new ModLoadingWarning(
                    modInfo, stage, i18nMessage,
                    exception.getMessage(),
                    exception.getClass().getSimpleName()
            );

            ModLoader.get().addWarning(warning);

            TwitchSpawn.LOGGER.error(exception.getMessage());
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String delimiter = "";
        for (Exception exception : exceptions) {
            sb.append(delimiter);
            sb.append(exception.getMessage());
            delimiter = "\n";
        }
        return sb.toString();
    }

}
