package net.programmer.igoodie.twitchspawn.events;


import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.resources.ResourceLocation;


@Environment(EnvType.CLIENT)
public interface TwitchSpawnClientGuiEvent
{
    Event<RenderDebugHud> DEBUG_TEXT = EventFactory.createLoop();

    Event<OverlayRenderPre> OVERLAY_RENDER_PRE = EventFactory.createLoop();

    Event<OverlayRenderPost> OVERLAY_RENDER_POST = EventFactory.createLoop();


    @Environment(EnvType.CLIENT)
    interface RenderDebugHud
    {
        void renderHud(GuiGraphics graphics);
    }


    @Environment(EnvType.CLIENT)
    interface OverlayRenderPre
    {
        void renderHud(GuiGraphics graphics, ResourceLocation overlay);
    }


    @Environment(EnvType.CLIENT)
    interface OverlayRenderPost
    {
        void renderHud(GuiGraphics graphics, ResourceLocation overlay);
    }
}
