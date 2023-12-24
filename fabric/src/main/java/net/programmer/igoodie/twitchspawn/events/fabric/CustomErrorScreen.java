//
// Created by BONNe
// Copyright - 2023
//


package net.programmer.igoodie.twitchspawn.events.fabric;


import com.electronwill.nightconfig.core.io.ParsingException;
import com.google.gson.JsonSyntaxException;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.programmer.igoodie.twitchspawn.mixin.fabric.ScreenAccessor;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;


/**
 * Very simplistic error screen for fabric.
 */
public class CustomErrorScreen extends ErrorScreen
{
    private final List<Exception> modLoadErrors;

    private Component errorHeader;


    public CustomErrorScreen(List<Exception> warnings)
    {
        super(Component.literal("Loading Error"), Component.literal("Twitch Spawn"));
        this.modLoadErrors = warnings;
    }


    @Override
    public void init()
    {
        super.init();
        this.clearWidgets();

        this.errorHeader = Component.literal(ChatFormatting.WHITE + "Twitch Spawn Loading Error" + ChatFormatting.RESET);

        MutableComponent component = Component.empty();

        this.modLoadErrors.forEach(exception ->
        {
            String i18nMessage;

            if (exception instanceof TSLSyntaxError)
            {
                i18nMessage = "modloader.twitchspawn.error.tsl";
            }
            else if (exception instanceof ParsingException)
            {
                i18nMessage = "modloader.twitchspawn.error.toml";
            }
            else if (exception instanceof JsonSyntaxException)
            {
                i18nMessage = "modloader.twitchspawn.error.json";
            }
            else
            {
                i18nMessage = "modloader.twitchspawn.error.unknown";
            }

            component.append(Component.translatable(i18nMessage)).
                append(Component.literal(exception.getMessage())).
                append("\n");
        });

        MultiLineTextWidget multiLineTextWidget = new MultiLineTextWidget(10, 45, component, this.font);

        this.addRenderableWidget(multiLineTextWidget);
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        drawMultiLineCenteredString(guiGraphics, font, errorHeader, this.width / 2, 10);
        ((ScreenAccessor) this).getRenderables().forEach(button -> button.render(guiGraphics, mouseX, mouseY, partialTick));
    }


    private void drawMultiLineCenteredString(GuiGraphics guiGraphics, Font fr, Component str, int x, int y)
    {
        for (FormattedCharSequence s : fr.split(str, this.width))
        {
            guiGraphics.drawString(fr, s, (int) (x - fr.width(s) / 2.0), (int) y, 0xFFFFFF, true);
            y += fr.lineHeight;
        }
    }
}
