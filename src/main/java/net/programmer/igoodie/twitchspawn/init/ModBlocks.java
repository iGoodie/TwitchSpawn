package net.programmer.igoodie.twitchspawn.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.block.TwitchSpawnJellyBlock;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBlocks {

    public static TwitchSpawnJellyBlock TWITCHSPAWN_JELLY_BLOCK;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        TWITCHSPAWN_JELLY_BLOCK = new TwitchSpawnJellyBlock(BlockBehaviour.Properties
                .of(Material.CLAY, MaterialColor.COLOR_PURPLE)
                .friction(0.8F)
                .sound(SoundType.SLIME_BLOCK)
                .noOcclusion());
        TWITCHSPAWN_JELLY_BLOCK.setRegistryName(TwitchSpawn.id("twitchspawn_jelly_block"));
        event.getRegistry().register(TWITCHSPAWN_JELLY_BLOCK);
    }

    @SubscribeEvent
    public static void registerBlockItems(RegistryEvent.Register<Item> event) {
        BlockItem twitchspawnJellyBlockItem = new BlockItem(TWITCHSPAWN_JELLY_BLOCK, new Item.Properties());
        twitchspawnJellyBlockItem.setRegistryName(TwitchSpawn.id("twitchspawn_jelly_block"));
        event.getRegistry().register(twitchspawnJellyBlockItem);
    }

}
