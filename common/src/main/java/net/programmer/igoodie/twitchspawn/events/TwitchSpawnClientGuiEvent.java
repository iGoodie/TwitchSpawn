package net.programmer.igoodie.twitchspawn.events;


import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;


@Environment(EnvType.CLIENT)
public interface TwitchSpawnClientGuiEvent
{
    /**
     * This event is fired when the debug text is rendered.
     */
    Event<RenderDebugHud> DEBUG_TEXT = EventFactory.createLoop();

    /**
     * This event is fired before overlay is rendered.
     */
    Event<OverlayRenderPre> OVERLAY_RENDER_PRE = EventFactory.createLoop();

    /**
     * This event is fired after overlay is rendered.
     */
    Event<OverlayRenderPost> OVERLAY_RENDER_POST = EventFactory.createLoop();

    /**
     * This event is fired after loading screen is removed.
     */
    Event<LoadingScreenFinish> FINISH_LOADING_OVERLAY = EventFactory.createLoop();


    /**
     * The forge event is {@link net.minecraftforge.client.event.CustomizeGuiOverlayEvent.DebugText}
     * The fabric triggers it via {@link net.programmer.igoodie.twitchspawn.mixin.fabric.MixinGui#renderDebugText}
     */
    @Environment(EnvType.CLIENT)
    interface RenderDebugHud
    {
        void renderHud(GuiGraphics graphics);
    }


    /**
     * The forge event is {@link net.minecraftforge.client.event.RenderGuiOverlayEvent.Pre}
     * The fabric triggers it via {@link net.programmer.igoodie.twitchspawn.mixin.fabric.MixinGui#preRenderHotbar}
     * and {@link net.programmer.igoodie.twitchspawn.mixin.fabric.MixinGui#preRenderHotbarSpectator}
     */
    @Environment(EnvType.CLIENT)
    interface OverlayRenderPre
    {
        void renderHud(GuiGraphics graphics, ResourceLocation overlay);
    }


    /**
     * This event is fired when the debug text is rendered.
     * The forge event is {@link net.minecraftforge.client.event.RenderGuiOverlayEvent.Post}
     * The fabric triggers it via {@link net.programmer.igoodie.twitchspawn.mixin.fabric.MixinGui#postRenderHotbar}
     * and {@link net.programmer.igoodie.twitchspawn.mixin.fabric.MixinGui#postRenderHotbarSpectator}
     */
    @Environment(EnvType.CLIENT)
    interface OverlayRenderPost
    {
        void renderHud(GuiGraphics graphics, ResourceLocation overlay);
    }


    /**
     * This event is fired when the debug text is rendered.
     * The forge triggers it via {@link net.programmer.igoodie.twitchspawn.mixin.forge.MixinLoadingOverlay#overlayRemoveEvent}
     * The fabric triggers it via {@link net.programmer.igoodie.twitchspawn.mixin.fabric.MixinLoadingOverlay#overlayRemoveEvent}
     */
    @Environment(EnvType.CLIENT)
    interface LoadingScreenFinish
    {
        void removeOverlay(Minecraft client, Screen screen);
    }
}
