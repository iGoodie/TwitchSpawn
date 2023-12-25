//
// Created by BONNe
// Copyright - 2023
//


package net.programmer.igoodie.twitchspawn.client.gui.screens;


import com.electronwill.nightconfig.core.io.ParsingException;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.util.List;
import java.util.Objects;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.programmer.igoodie.twitchspawn.TwitchSpawnLoadingErrors;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;


/**
 * Very simplistic error screen for fabric.
 */
public class LoadingErrorScreen extends Screen
{
    /**
     * Creates a new error screen with the given exceptions.
     * @param configLoadingExceptions The exceptions to display.
     */
    public LoadingErrorScreen(List<Exception> configLoadingExceptions)
    {
        super(Component.literal("Loading Error"));
        this.configLoadingExceptions = configLoadingExceptions;
    }


    @Override
    public void init()
    {
        super.init();
        this.clearWidgets();

        this.errorScreenTitle = Component.translatable("modloader.twitchspawn.error.title").
            withStyle(ChatFormatting.YELLOW);

        this.addRenderableWidget(Button.builder(
            Component.translatable("modloader.twitchspawn.error.folder"),
                onPress -> Util.getPlatform().openFile(new File(ConfigManager.CONFIG_DIR_PATH))).
            size(this.width / 2 - 55, 20).
            pos(50, this.height - 46).
            build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("modloader.twitchspawn.error.reload"),
                onPress -> reloadConfigs()).
            size(this.width / 2 - 55, 20).
            pos(this.width / 2 + 5, this.height - 46).
            build());
        this.addRenderableWidget(Button.builder(
            Component.translatable("modloader.twitchspawn.error.continue"),
                onPress -> this.minecraft.setScreen(null)).
            size(this.width / 2, 20).
            pos(this.width / 4, this.height - 24).
            build());

        this.entryList = new LoadingEntryList(this, this.configLoadingExceptions);
        this.addWidget(this.entryList);
        this.setFocused(this.entryList);
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.entryList.render(guiGraphics, mouseX, mouseY, partialTick);
        drawMultiLineCenteredString(guiGraphics,
            font,
            errorScreenTitle,
            this.width / 2,
            10);
    }


    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f)
    {
        guiGraphics.fillGradient(0, 0, this.width, this.height, -12574688, -11530224);
    }


    /**
     * This method reloads configs and repopulates list with new exceptions or close screen if everything
     * is correct.
     */
    private void reloadConfigs()
    {
        try
        {
            ConfigManager.loadConfigs();
            this.minecraft.setScreen(null);
        }
        catch (TwitchSpawnLoadingErrors e)
        {
            this.configLoadingExceptions.clear();
            this.configLoadingExceptions.addAll(e.getExceptions());
            this.init();
        }
    }


    /**
     * This method draws multiline centered string.
     * @param guiGraphics GuiGraphics instance.
     * @param font FontRenderer instance.
     * @param component String to draw.
     * @param x X coordinate.
     * @param y Y coordinate.
     */
    private void drawMultiLineCenteredString(GuiGraphics guiGraphics, Font font, Component component, int x, int y)
    {
        for (FormattedCharSequence chars : font.split(component, this.width))
        {
            guiGraphics.drawString(font, chars, (int) ((x - font.width(chars) / 2.0)), y, 0xFFFFFF, true);
            y += font.lineHeight;
        }
    }


    /**
     * This class contains and renders all error entries.
     */
    private static class LoadingEntryList extends ObjectSelectionList<LoadingEntryList.LoadingMessageEntry>
    {
        LoadingEntryList(final LoadingErrorScreen parent, final List<Exception> errors)
        {
            super(parent.minecraft,
                parent.width,
                parent.height,
                35,
                parent.height - 50,
                errors.stream().mapToInt(warning ->
                    parent.font.split(Component.literal(warning.getMessage()), parent.width - 20).size()).
                    max().
                    orElse(0) * parent.minecraft.font.lineHeight + 8);

            errors.forEach(exception -> {
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

                this.addEntry(new LoadingMessageEntry(
                    Component.translatable(i18nMessage, exception.getMessage(), exception.getClass().toString())));
            });
        }


        @Override
        protected int getScrollbarPosition()
        {
            return this.width - 6;
        }


        @Override
        public int getRowWidth()
        {
            return this.width;
        }


        public class LoadingMessageEntry extends ObjectSelectionList.Entry<LoadingMessageEntry>
        {
            private final Component message;

            LoadingMessageEntry(final Component message)
            {
                this.message = Objects.requireNonNull(message);
            }


            @Override
            public Component getNarration()
            {
                return Component.translatable("narrator.select", message);
            }


            @Override
            public void render(GuiGraphics guiGraphics,
                int entryIdx,
                int top,
                int left,
                final int entryWidth,
                final int entryHeight,
                final int mouseX,
                final int mouseY,
                final boolean p_194999_5_,
                final float partialTick)
            {
                Font font = Minecraft.getInstance().font;
                var strings = font.split(message, LoadingEntryList.this.width - 20);
                int y = top + 2;

                for (var string : strings)
                {
                    guiGraphics.drawString(font, string, left + 5, y, 0xFFFFFF, false);
                    y += font.lineHeight;
                }
            }
        }
    }


    /**
     * List of config exceptions
     */
    private final List<Exception> configLoadingExceptions;

    /**
     * The component that displays error entries.
     */
    private LoadingEntryList entryList;

    /**
     * The title of the error screen.
     */
    private Component errorScreenTitle;
}
