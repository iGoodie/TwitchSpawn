package net.programmer.igoodie.twitchspawn.registries;


import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.command.RulesetNameArgumentType;
import net.programmer.igoodie.twitchspawn.command.StreamerArgumentType;
import net.programmer.igoodie.twitchspawn.command.TSLWordsArgumentType;


/**
 * This class registers argument types.
 */
@Mod.EventBusSubscriber(modid = TwitchSpawn.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TwitchSpawnAugmentTypes
{
    /**
     * Registry for argument types.
     */
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> REGISTRY =
        DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, TwitchSpawn.MOD_ID);

    static {
        // Argument type for ruleset names.
        REGISTRY.register("ruleset", () -> ArgumentTypeInfos.registerByClass(RulesetNameArgumentType.class,
            SingletonArgumentInfo.contextFree(RulesetNameArgumentType::rulesetName)));

        // Argument type for streamer names.
        REGISTRY.register("streamer", () -> ArgumentTypeInfos.registerByClass(StreamerArgumentType.class,
            SingletonArgumentInfo.contextFree(StreamerArgumentType::streamerNick)));

        // Argument type for TSL words.
        REGISTRY.register("tslwords", () -> ArgumentTypeInfos.registerByClass(TSLWordsArgumentType.class,
            SingletonArgumentInfo.contextFree(TSLWordsArgumentType::tslWords)));
    }
}
