//
// Created by BONNe
// Copyright - 2023
//


package net.programmer.igoodie.twitchspawn.registries.fabric;


import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.resources.ResourceLocation;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.command.RulesetNameArgumentType;
import net.programmer.igoodie.twitchspawn.command.StreamerArgumentType;
import net.programmer.igoodie.twitchspawn.command.TSLWordsArgumentType;


/**
 * This class manages the registration of command argument types.
 */
public class TwitchSpawnArgumentTypesImpl
{
    /**
     * Fabric has very simple command argument type registration.
     */
    public static void registerArgumentType()
    {
        ArgumentTypeRegistry.registerArgumentType(
            new ResourceLocation(TwitchSpawn.MOD_ID, "ruleset"),
            RulesetNameArgumentType.class,
            SingletonArgumentInfo.contextFree(RulesetNameArgumentType::rulesetName));

        ArgumentTypeRegistry.registerArgumentType(
            new ResourceLocation(TwitchSpawn.MOD_ID, "streamer"),
            StreamerArgumentType.class,
            SingletonArgumentInfo.contextFree(StreamerArgumentType::streamerNick));

        ArgumentTypeRegistry.registerArgumentType(
            new ResourceLocation(TwitchSpawn.MOD_ID, "tslwords"),
            TSLWordsArgumentType.class,
            SingletonArgumentInfo.contextFree(TSLWordsArgumentType::tslWords));
    }
}
