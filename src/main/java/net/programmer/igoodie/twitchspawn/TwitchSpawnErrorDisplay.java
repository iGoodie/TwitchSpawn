package net.programmer.igoodie.twitchspawn;

import com.electronwill.nightconfig.core.io.ParsingException;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

@SideOnly(Side.CLIENT)
public class TwitchSpawnErrorDisplay extends CustomModLoadingErrorDisplayException {

    TwitchSpawnLoadingErrors loadingErrors;

    public TwitchSpawnErrorDisplay(TwitchSpawnLoadingErrors loadingErrors) {
        this.loadingErrors = loadingErrors;
    }

    @Override
    public void initGui(GuiErrorScreen errorScreen, FontRenderer fontRenderer) {}

    @Override
    public void drawScreen(GuiErrorScreen errorScreen, FontRenderer fontRenderer, int mouseRelX, int mouseRelY, float tickTime) {
        int x = errorScreen.width / 2;
        int y = (int) (errorScreen.height * 0.1f);

        String title = new TextComponentTranslation("modloader.twitchspawn.error.title").getFormattedText();
        errorScreen.drawCenteredString(fontRenderer, title, x, y, 0xFF_FFFFFF);

        y += 20;

        for (Exception exception : loadingErrors.exceptions) {
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

            String[] lines = message.split("\r?\n");
            for (String line : lines) {
                errorScreen.drawCenteredString(fontRenderer, line, x, y, 0xFF_FFFFFF);
                y += 10;
            }
            y += 10;
        }
    }

}
