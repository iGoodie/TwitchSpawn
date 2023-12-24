package net.programmer.igoodie.twitchspawn.events.forge;


import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.programmer.igoodie.twitchspawn.events.TwitchSpawnClientGuiEvent;


/**
 * This class manages the registration of events.
 */
public class TwitchSpawnEventHandlerClientImpl
{
    @SubscribeEvent
    public static void onScreenGuiOverlayEvent(CustomizeGuiOverlayEvent.DebugText event)
    {
        TwitchSpawnClientGuiEvent.DEBUG_TEXT.invoker().renderHud(event.getGuiGraphics());
    }

    @SubscribeEvent
    public static void onRenderGuiOverlayPre(RenderGuiOverlayEvent.Pre event)
    {
        TwitchSpawnClientGuiEvent.OVERLAY_RENDER_PRE.invoker().renderHud(event.getGuiGraphics(), event.getOverlay().id());
    }

    @SubscribeEvent
    public static void onRenderGuiOverlayPost(RenderGuiOverlayEvent.Post event)
    {
        TwitchSpawnClientGuiEvent.OVERLAY_RENDER_POST.invoker().renderHud(event.getGuiGraphics(), event.getOverlay().id());
    }
}
