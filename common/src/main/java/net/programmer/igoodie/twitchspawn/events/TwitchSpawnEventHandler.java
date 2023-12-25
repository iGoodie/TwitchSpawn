package net.programmer.igoodie.twitchspawn.events;


import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;


public final class TwitchSpawnEventHandler
{
    private TwitchSpawnEventHandler()
    {
    }


    private static boolean initialized = false;


    public static void init()
    {
        if (initialized) return;

        initialized = true;

        if (Platform.getEnvironment() == Env.CLIENT)
        {
            registerClient();
        }
    }


    @ExpectPlatform
    @Environment(EnvType.CLIENT)
    private static void registerClient()
    {
        throw new AssertionError();
    }
}
