package net.programmer.igoodie.twitchspawn.registries;


import dev.architectury.injectables.annotations.ExpectPlatform;


/**
 * This class registers argument types.
 */
public class TwitchSpawnArgumentTypes
{
    @ExpectPlatform
    public static void registerArgumentType()
    {
        // Just throw an error, the content should get replaced at runtime.
        throw new AssertionError();
    }
}
