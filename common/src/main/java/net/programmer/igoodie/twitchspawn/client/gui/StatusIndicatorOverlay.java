package net.programmer.igoodie.twitchspawn.client.gui;


import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.configuration.PreferencesConfig;
import net.programmer.igoodie.twitchspawn.registries.TwitchSpawnSoundEvent;


public class StatusIndicatorOverlay {

    private static final ResourceLocation indicatorIcons =
            new ResourceLocation(TwitchSpawn.MOD_ID, "textures/indicators.png");

    private static boolean running = false;
    private static boolean drew = false;

    public static void setRunning(boolean running) {
        StatusIndicatorOverlay.running = running;

        ResourceLocation soundEvent = running ?
            TwitchSpawnSoundEvent.POP_IN.getId() : TwitchSpawnSoundEvent.POP_OUT.getId();

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer self = minecraft.player;

        if (self != null) { // Here to hopefully fix an obscure Null Pointer (From UNKNOWN PENGUIN's log)
            self.playSound(SoundEvent.createVariableRangeEvent(soundEvent), 1f, 1f);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;
    }

    @SubscribeEvent
    public static void onRenderGuiPre(RenderGuiOverlayEvent.Pre event) {
        drew = false;
    }

    @SubscribeEvent
    public static void onRenderGuiPost(CustomizeGuiOverlayEvent.DebugText event) {
        if (ConfigManager.PREFERENCES.indicatorDisplay == PreferencesConfig.IndicatorDisplay.DISABLED)
            return; // The display is disabled, stop here

        // Already drew, stop here
        if (drew) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, indicatorIcons);

        GuiGraphics gui = event.getGuiGraphics();
        gui.pose().pushPose();

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

        gui.pose().scale(1f, 1f, 1f);
        gui.blit(indicatorIcons, x, y, ux, uy, w, h);

        gui.pose().popPose();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
//        RenderSystem.setShaderTexture(0, Gui.GUI_ICONS_LOCATION);

        drew = true;
    }

}
