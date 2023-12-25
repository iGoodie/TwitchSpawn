
package net.programmer.igoodie.twitchspawn.events.forge;


import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.programmer.igoodie.twitchspawn.events.TwitchSpawnClientGuiEvent;


/**
 * This class handles the registration of events.
 */
public class TwitchSpawnEventHandlerImpl
{
    @OnlyIn(Dist.CLIENT)
    public static void registerClient()
    {
        // Register client events.
        MinecraftForge.EVENT_BUS.register(TwitchSpawnEventHandlerImpl.class);
    }


    @SubscribeEvent
    public static void onScreenGuiOverlayEvent(CustomizeGuiOverlayEvent.DebugText event)
    {
        TwitchSpawnClientGuiEvent.DEBUG_TEXT.invoker().renderHud(event.getGuiGraphics());
    }


    @SubscribeEvent
    public static void onRenderGuiOverlayPre(RenderGuiOverlayEvent.Pre event)
    {
        TwitchSpawnClientGuiEvent.OVERLAY_RENDER_PRE.invoker().renderHud(event.getGuiGraphics(),
            event.getOverlay().id());
    }


    @SubscribeEvent
    public static void onRenderGuiOverlayPost(RenderGuiOverlayEvent.Post event)
    {
        TwitchSpawnClientGuiEvent.OVERLAY_RENDER_POST.invoker().renderHud(event.getGuiGraphics(),
            event.getOverlay().id());
    }
}
