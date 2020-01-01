package net.programmer.igoodie.twitchspawn.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.configuration.ConfigManager;
import net.programmer.igoodie.twitchspawn.configuration.PreferencesConfig;

@SideOnly(Side.CLIENT)
public class StatusIndicatorOverlay {

    private static final ResourceLocation indicatorIcons =
            new ResourceLocation(TwitchSpawn.MOD_ID, "textures/indicators.png");

    private static boolean running = false;
    private static boolean drew = false;

    public static void setRunning(boolean running) {
        StatusIndicatorOverlay.running = running;

        String soundName = running ? "pop_in" : "pop_out";

        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayerSP self = minecraft.player;

        if (self != null) { // Here to hopefully fix an obscure Null Pointer (From UNKNOWN PENGUIN's log)
            self.playSound(new SoundEvent(new ResourceLocation(TwitchSpawn.MOD_ID, soundName)), 1f, 1f);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;
    }

    @SubscribeEvent
    public static void onRenderGuiPre(RenderGameOverlayEvent.Pre event) {
        drew = false;
    }

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGameOverlayEvent.Post event) {
        if (ConfigManager.PREFERENCES.indicatorDisplay == PreferencesConfig.IndicatorDisplay.DISABLED)
            return; // The display is disabled, stop here

        Minecraft minecraft = Minecraft.getMinecraft();

        if (event.getType() != ElementType.HOTBAR)
            return; // Render only on HOTBAR

        // Already drew, stop here
        if (drew) return;

        minecraft.getTextureManager().bindTexture(indicatorIcons);
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();

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

        GlStateManager.scale(1f, 1f, 1f);
        minecraft.ingameGUI.drawTexturedModalRect(x, y, ux, uy, w, h);

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        minecraft.getTextureManager().bindTexture(Gui.ICONS);

        drew = true;
    }

}
