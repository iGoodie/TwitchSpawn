package net.programmer.igoodie.twitchspawn.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import org.lwjgl.opengl.GL11;

public class StatusIndicatorOverlay {

    private static final ResourceLocation indicatorIcons =
            new ResourceLocation(TwitchSpawn.MOD_ID, "textures/indicators.png");

    private static boolean running = false;
    private static boolean drew = false;

    public static void setRunning(boolean running) {
        StatusIndicatorOverlay.running = running;
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
        Minecraft minecraft = Minecraft.getInstance();

        if (event.getType() != ElementType.HOTBAR)
            return;

        if (drew)
            return;

        minecraft.getTextureManager().bindTexture(indicatorIcons);
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();

        int x = 5, y = 5;
        int ux = 0, uy = running ? 22 : 0;
        int w = 65, h = 22;

        GlStateManager.scalef(1f, 1f, 1f);
        minecraft.ingameGUI.blit(x, y, ux, uy, w, h);

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        minecraft.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
//        minecraft.fontRenderer.drawStringWithShadow(debugMessage, 2, 2, 0xFF_FFFFFF);

        drew = true;
    }

    public static void enableAlpha(float alpha) {
        GlStateManager.enableBlend();

        if (alpha == 1f)
            return;

        GlStateManager.color4f(1.0F, 1.0F, 1.0F, alpha);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    public static void disableAlpha(float alpha) {
        GlStateManager.disableBlend();

        if (alpha == 1f)
            return;

        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

}
