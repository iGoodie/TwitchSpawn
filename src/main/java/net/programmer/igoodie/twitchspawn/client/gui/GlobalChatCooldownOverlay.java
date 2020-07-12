package net.programmer.igoodie.twitchspawn.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.configuration.PreferencesConfig;
import net.programmer.igoodie.twitchspawn.util.CooldownBucket;

public class GlobalChatCooldownOverlay {

    private static final ResourceLocation cooldownGlyphs =
            new ResourceLocation(TwitchSpawn.MOD_ID, "textures/cooldown.png");

    private static long timestamp = -1;
    private static boolean drew = false;

    public static void setCooldownTimestamp(long timestamp) {
        GlobalChatCooldownOverlay.timestamp = timestamp;
    }

    @SubscribeEvent
    public static void onRenderGuiPre(RenderGameOverlayEvent.Pre event) {
        drew = false;
    }

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGameOverlayEvent.Post event) {
        if (event.getType() != ElementType.HOTBAR)
            return; // Render only on HOTBAR

        // Already drew, stop here
        if (drew) return;

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

            MatrixStack matrixStack = event.getMatrixStack();

            matrixStack.push();
            matrixStack.scale(scale, scale, scale);
            renderGlyph(matrixStack, String.format("%02d", minutes), x, (int) (y / scale));
            renderGlyph(matrixStack, ":", x + 32, (int) (y / scale));
            renderGlyph(matrixStack, String.format("%02d", seconds), (x + 10 + 2 * 18), (int) (y / scale));
            renderGlyph(matrixStack, "i", (int) (x + 10 + 4.25f * 18), (int) ((y - 2) / scale));
            matrixStack.pop();
        }

        drew = true;
    }

    public static void renderGlyph(MatrixStack ms, String number, int x, int y) {
        Minecraft minecraft = Minecraft.getInstance();

        minecraft.getTextureManager().bindTexture(cooldownGlyphs);
        ms.push();
        GlStateManager.enableBlend();

        if (number.equals("i")) {
            int ux = 0;
            int uy = 20;
            int w = 28;
            int h = 25;

            minecraft.ingameGUI.blit(
                    ms,
                    x, y,
                    ux, uy,
                    w, h
            );

        } else {
            char[] chars = number.toCharArray();

            for (int i = 0, offset = 0; i < chars.length; i++) {
                int digit = number.equals(":") ? 10
                        : Character.digit(number.charAt(i), 10);

                int ux = 18 * digit;
                int uy = 0;
                int w = 18;
                int h = 18;

                minecraft.ingameGUI.blit(
                        ms,
                        x + offset, y,
                        ux, uy,
                        w, h
                );

                offset += w;
            }
        }

        GlStateManager.disableBlend();
        ms.pop();
        minecraft.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
    }

}
