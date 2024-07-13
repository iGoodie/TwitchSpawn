package net.programmer.igoodie.twitchspawn.init;

import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.item.TwitchSpawnJellyItem;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItems {

    public static Item TWITCH_LOGO;
    public static TwitchSpawnJellyItem TWITCHSPAWN_JELLY;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        TWITCH_LOGO = new Item(new Item.Properties());
        TWITCH_LOGO.setRegistryName(TwitchSpawn.id("twitch_logo"));
        event.getRegistry().register(TWITCH_LOGO);

        TWITCHSPAWN_JELLY = new TwitchSpawnJellyItem(new Item.Properties());
        TWITCHSPAWN_JELLY.setRegistryName(TwitchSpawn.id("twitchspawn_jelly"));
        event.getRegistry().register(TWITCHSPAWN_JELLY);
    }

}
