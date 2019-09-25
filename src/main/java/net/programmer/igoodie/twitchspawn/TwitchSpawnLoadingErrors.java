package net.programmer.igoodie.twitchspawn;

import com.electronwill.nightconfig.core.io.ParsingException;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxErrors;

import java.util.LinkedList;
import java.util.List;

public class TwitchSpawnLoadingErrors extends RuntimeException {

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

    public void display() {
        if (FMLLaunchHandler.side() == Side.CLIENT)
            displayForClient(); // Decoupled to eliminate Class loading errors on Server side

        if (FMLLaunchHandler.side() == Side.SERVER) {
            String title = new TextComponentTranslation("modloader.twitchspawn.error.title").getFormattedText();

            TwitchSpawn.LOGGER.error(title);

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

                String g0 = exception.getMessage();
                String g1 = exception.getClass().getSimpleName();
                String message = new TextComponentTranslation(i18nMessage, null, g0, g1).getFormattedText();
                message = message.replaceFirst("\r?\n", " - ");

                TwitchSpawn.LOGGER.error(message);
            }

            throw new RuntimeException("Error on loading TwitchSpawn");
        }
    }

    @SideOnly(Side.CLIENT)
    private void displayForClient() {
        throw new TwitchSpawnErrorDisplay(this);
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
