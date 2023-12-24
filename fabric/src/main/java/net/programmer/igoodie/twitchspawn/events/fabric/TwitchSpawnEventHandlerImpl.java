//
// Created by BONNe
// Copyright - 2023
//


package net.programmer.igoodie.twitchspawn.events.fabric;


import dev.architectury.event.events.common.LifecycleEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.Overlay;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.events.TwitchSpawnCommonEvent;


/**
 * This class manages the registration of events.
 */
public class TwitchSpawnEventHandlerImpl
{
    /**
     * Register client events.
     */
    public static void registerClient()
    {
        // Register error for incorrect configs.
        TwitchSpawnCommonEvent.SETUP_EVENT.register(exception -> {
            ClientLifecycleEvents.CLIENT_STARTED.register(client ->
                Minecraft.getInstance().setOverlay(new Overlay()
                {
                    @Override
                    public void render(GuiGraphics guiGraphics, int i, int j, float f)
                    {
                        ErrorScreen errorScreen = new CustomErrorScreen(exception.getExceptions());
                        errorScreen.init(client,
                            client.getWindow().getGuiScaledWidth(),
                            client.getWindow().getGuiScaledHeight());
                        errorScreen.render(guiGraphics, i, j, f);
                    }
                }));
        });
    }
}
