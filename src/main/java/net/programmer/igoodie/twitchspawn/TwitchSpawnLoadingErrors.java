package net.programmer.igoodie.twitchspawn;

import com.electronwill.nightconfig.core.io.ParsingException;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.chat.Component;
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

    public void logErrors() {
        for (Exception exception : exceptions) {
            TwitchSpawn.LOGGER.error(exception.getMessage());
        }
    }

    public List<Component> toWarningMessages() {
        List<Component> messages = new LinkedList<>();

        for (Exception exception : exceptions) {
            String i18nMessage;

            if (exception instanceof TSLSyntaxError)
                i18nMessage = "twitchspawn.error.tsl";
            else if (exception instanceof ParsingException)
                i18nMessage = "twitchspawn.error.toml";
            else if (exception instanceof JsonSyntaxException)
                i18nMessage = "twitchspawn.error.json";
            else
                i18nMessage = "twitchspawn.error.unknown";

            messages.add(Component.translatable(i18nMessage,
                    exception.getMessage(),
                    exception.getClass().getSimpleName()));
        }

        return messages;
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
