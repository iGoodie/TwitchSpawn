//
// Created by BONNe
// Copyright - 2023
//


package net.programmer.igoodie.twitchspawn.mixin.fabric;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;


/**
 * Accessor for screen rendables variable
 */
@Mixin(Screen.class)
public interface ScreenAccessor
{
    @Accessor
    List<Renderable> getRenderables();
}
