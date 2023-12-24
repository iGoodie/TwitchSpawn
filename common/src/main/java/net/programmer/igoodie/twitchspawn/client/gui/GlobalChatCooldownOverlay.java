package net.programmer.igoodie.twitchspawn.client.gui;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientGuiEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.configuration.PreferencesConfig;
import net.programmer.igoodie.twitchspawn.events.TwitchSpawnClientGuiEvent;
import net.programmer.igoodie.twitchspawn.util.CooldownBucket;

public class GlobalChatCooldownOverlay {

    private static final ResourceLocation cooldownGlyphs =
            new ResourceLocation(TwitchSpawn.MOD_ID, "textures/cooldown.png");

    private static long timestamp = -1;

    private static boolean drew = false;

    /**
     * Render indicator
     */
    private static final TwitchSpawnClientGuiEvent.OverlayRenderPre PRE_RENDER =
        (graphics, resourceLocation) -> drew = false;

    /**
     * Render the gui
     */
    private static final TwitchSpawnClientGuiEvent.OverlayRenderPost POST_RENDER =
        GlobalChatCooldownOverlay::onRenderGuiPost;


    /**
     * Register rendering events.
     */
    public static void register() {
        TwitchSpawnClientGuiEvent.OVERLAY_RENDER_PRE.register(PRE_RENDER);
        TwitchSpawnClientGuiEvent.OVERLAY_RENDER_POST.register(POST_RENDER);
    }


    /**
     * Unregister rendering events.
     */
    public static void unregister() {
        TwitchSpawnClientGuiEvent.OVERLAY_RENDER_PRE.unregister(PRE_RENDER);
        TwitchSpawnClientGuiEvent.OVERLAY_RENDER_POST.unregister(POST_RENDER);
    }


    public static void setCooldownTimestamp(long timestamp) {
        GlobalChatCooldownOverlay.timestamp = timestamp;
    }


    private static void onRenderGuiPost(GuiGraphics graphics, ResourceLocation resourceLocation) {
        if (!resourceLocation.equals(ResourceLocation.of("hotbar", ':')))
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

            PoseStack matrixStack = graphics.pose();
            matrixStack.pushPose();
            matrixStack.scale(scale, scale, scale);
            renderGlyph(graphics, matrixStack, String.format("%02d", minutes), x, (int) (y / scale));
            renderGlyph(graphics, matrixStack, ":", x + 32, (int) (y / scale));
            renderGlyph(graphics, matrixStack, String.format("%02d", seconds), (x + 10 + 2 * 18), (int) (y / scale));
            renderGlyph(graphics, matrixStack, "i", (int) (x + 10 + 4.25f * 18), (int) ((y - 2) / scale));
            matrixStack.popPose();
        }

        drew = true;
    }

    private static void renderGlyph(GuiGraphics guiGraphics, PoseStack ms, String number, int x, int y) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, cooldownGlyphs);

        ms.pushPose();

        RenderSystem.enableBlend();

        if (number.equals("i")) {
            int ux = 0;
            int uy = 20;
            int w = 28;
            int h = 25;

            guiGraphics.blit(ResourceLocation.tryParse("hotbar"),
                x, y,
                ux, uy,
                w, h
            );

        } else {
            char[] chars = number.toCharArray();

            for (int i = 0, offset = 0; i < chars.length; i++) {
                int digit = number.equals(":") ? 10 : Character.digit(number.charAt(i), 10);

                int ux = 18 * digit;
                int uy = 0;
                int w = 18;
                int h = 18;

                guiGraphics.blit(ResourceLocation.tryParse("hotbar"),
                    x + offset, y,
                    ux, uy,
                    w, h
                );

                offset += w;
            }
        }

        RenderSystem.disableBlend();
        ms.popPose();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, new ResourceLocation("hud/hotbar"));
    }
}
