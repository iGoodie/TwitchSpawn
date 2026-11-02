package net.programmer.igoodie.twitchspawn.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.configuration.PreferencesConfig;
import net.programmer.igoodie.twitchspawn.util.CooldownBucket;

public class GlobalChatCooldownOverlay {

    private static final Identifier cooldownGlyphs =
            Identifier.fromNamespaceAndPath(TwitchSpawn.MOD_ID, "textures/cooldown.png");

    private static long timestamp = -1;

    public static void setCooldownTimestamp(long timestamp) {
        GlobalChatCooldownOverlay.timestamp = timestamp;
    }

    public static void render(GuiGraphicsExtractor graphics) {
        long cooldown = (timestamp - CooldownBucket.now()) / 1000;

        if (cooldown > 0) {
            int minutes = Math.min(99, Math.max(0, (int) (cooldown / 60)));
            int seconds = Math.min(59, Math.max(0, (int) (cooldown % 60)));

            float scale = 0.5f;
            int x, y;

            if (ConfigManager.PREFERENCES.indicatorDisplay == PreferencesConfig.IndicatorDisplay.ENABLED) {
                x = 20;
                y = 30;

            } else if (ConfigManager.PREFERENCES.indicatorDisplay == PreferencesConfig.IndicatorDisplay.CIRCLE_ONLY) {
                x = 40;
                y = 6;

            } else {
                x = 10;
                y = 5;
            }

            graphics.pose().pushMatrix();
            graphics.pose().scale(scale, scale);
            renderGlyph(graphics, String.format("%02d", minutes), x, (int) (y / scale));
            renderGlyph(graphics, ":", x + 32, (int) (y / scale));
            renderGlyph(graphics, String.format("%02d", seconds), (x + 10 + 2 * 18), (int) (y / scale));
            renderGlyph(graphics, "i", (int) (x + 10 + 4.25f * 18), (int) ((y - 2) / scale));
            graphics.pose().popMatrix();
        }
    }

    public static void renderGlyph(GuiGraphicsExtractor graphics, String number, int x, int y) {
        if (number.equals("i")) {
            int ux = 0;
            int uy = 20;
            int w = 28;
            int h = 25;

            graphics.blit(RenderPipelines.GUI_TEXTURED, cooldownGlyphs, x, y, ux, uy, w, h, 256, 256);

        } else {
            char[] chars = number.toCharArray();

            for (int i = 0, offset = 0; i < chars.length; i++) {
                int digit = number.equals(":") ? 10
                        : Character.digit(number.charAt(i), 10);

                int ux = 18 * digit;
                int uy = 0;
                int w = 18;
                int h = 18;

                graphics.blit(RenderPipelines.GUI_TEXTURED, cooldownGlyphs, x + offset, y, ux, uy, w, h, 256, 256);

                offset += w;
            }
        }
    }

}
