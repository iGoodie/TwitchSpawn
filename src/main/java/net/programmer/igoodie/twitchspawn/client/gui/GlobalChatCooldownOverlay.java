package net.programmer.igoodie.twitchspawn.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.programmer.igoodie.twitchspawn.util.CooldownBucket;

public class GlobalChatCooldownOverlay {

    private static long timestamp = -1;
    private static boolean drew = false;

    public static void setCooldownTimestamp(long timestamp) {
        GlobalChatCooldownOverlay.timestamp = timestamp;
    }

//    @SubscribeEvent
//    public static void onClientTick(TickEvent.ClientTickEvent event) {
//
//    }

    @SubscribeEvent
    public static void onRenderGuiPre(RenderGameOverlayEvent.Pre event) {
        drew = false;
    }

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGameOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        long cooldown = (timestamp - CooldownBucket.now()) / 1000;

        if (cooldown >= 0) {
            String msg = String.format("Global Cooldown: %ssec", cooldown);
            minecraft.fontRenderer.drawString(msg, 10, 30, 0xFF_000000);
        }

        drew = true;
    }

}
