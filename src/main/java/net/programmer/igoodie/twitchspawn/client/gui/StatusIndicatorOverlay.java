package net.programmer.igoodie.twitchspawn.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.configuration.PreferencesConfig;

public class StatusIndicatorOverlay {

    private static final Identifier indicatorIcons =
            Identifier.fromNamespaceAndPath(TwitchSpawn.MOD_ID, "textures/indicators.png");

    private static boolean running = false;

    public static void setRunning(boolean running) {
        StatusIndicatorOverlay.running = running;

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer self = minecraft.player;

        if (self != null) { // Here to hopefully fix an obscure Null Pointer (From UNKNOWN PENGUIN's log)
            self.playSound(running ? TwitchSpawn.POP_IN : TwitchSpawn.POP_OUT, 1f, 1f);
        }
    }

    public static void render(GuiGraphicsExtractor graphics) {
        if (ConfigManager.PREFERENCES.indicatorDisplay == PreferencesConfig.IndicatorDisplay.DISABLED)
            return; // The display is disabled, stop here

        int x = 5, y = 5;
        int ux = -1, uy = -1;
        int w = -1, h = -1;

        if (ConfigManager.PREFERENCES.indicatorDisplay == PreferencesConfig.IndicatorDisplay.ENABLED) {
            ux = 0;
            uy = running ? 22 : 0;
            w = 65;
            h = 22;
        } else if (ConfigManager.PREFERENCES.indicatorDisplay == PreferencesConfig.IndicatorDisplay.CIRCLE_ONLY) {
            ux = 0;
            uy = running ? 56 : 44;
            w = 12;
            h = 12;
        }

        graphics.blit(RenderPipelines.GUI_TEXTURED, indicatorIcons, x, y, ux, uy, w, h, 256, 256);
    }

}
