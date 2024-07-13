package net.programmer.igoodie.twitchspawn.init;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModModels {

    @SubscribeEvent
    public static void onModelRegister(ModelRegistryEvent event) {
        registerBlockRenderTypes();
    }

    public static void registerBlockRenderTypes() {
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.TWITCHSPAWN_JELLY_BLOCK, RenderType.translucent());
    }

}
