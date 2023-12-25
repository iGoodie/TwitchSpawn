package net.programmer.igoodie.twitchspawn.registries.forge;


import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.command.RulesetNameArgumentType;
import net.programmer.igoodie.twitchspawn.command.StreamerArgumentType;
import net.programmer.igoodie.twitchspawn.command.TSLWordsArgumentType;


public class TwitchSpawnArgumentTypesImpl
{
    /**
     * Registry for argument types.
     */
    public static final net.minecraftforge.registries.DeferredRegister<ArgumentTypeInfo<?, ?>> REGISTRY =
        DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, TwitchSpawn.MOD_ID);

    public static void registerArgumentType()
    {
        // Do nothing. Forge is registred on startup.
    }

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
