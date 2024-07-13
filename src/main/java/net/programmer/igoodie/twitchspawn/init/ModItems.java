package net.programmer.igoodie.twitchspawn.init;

import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItems {

    public static Item TWITCH_LOGO;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        TWITCH_LOGO = new Item(new Item.Properties());
        TWITCH_LOGO.setRegistryName(TwitchSpawn.id("twitch_logo"));
        event.getRegistry().register(TWITCH_LOGO);
    }

}
