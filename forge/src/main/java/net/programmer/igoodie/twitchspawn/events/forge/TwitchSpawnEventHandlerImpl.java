
package net.programmer.igoodie.twitchspawn.events.forge;


import com.electronwill.nightconfig.core.io.ParsingException;
import com.google.gson.JsonSyntaxException;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.*;
import net.minecraftforge.forgespi.language.IModInfo;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.events.TwitchSpawnCommonEvent;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;


/**
 * This class handles the registration of events.
 */
public class TwitchSpawnEventHandlerImpl
{
    @OnlyIn(Dist.CLIENT)
    public static void registerClient()
    {
        // Register client events.
        MinecraftForge.EVENT_BUS.register(TwitchSpawnEventHandlerClientImpl.class);

        // Register error for incorrect configs.
        TwitchSpawnCommonEvent.SETUP_EVENT.register(error -> {
            ModLoadingStage stage = ModLoadingStage.COMMON_SETUP;

            ModContainer modContainer = ModList.get().getModContainerById(TwitchSpawn.MOD_ID).get();

            IModInfo modInfo = modContainer.getModInfo();

            for (Exception exception : error.getExceptions())
            {
                String i18nMessage;

                if (exception instanceof TSLSyntaxError)
                {
                    i18nMessage = "modloader.twitchspawn.error.tsl";
                }
                else if (exception instanceof ParsingException)
                {
                    i18nMessage = "modloader.twitchspawn.error.toml";
                }
                else if (exception instanceof JsonSyntaxException)
                {
                    i18nMessage = "modloader.twitchspawn.error.json";
                }
                else
                {
                    i18nMessage = "modloader.twitchspawn.error.unknown";
                }

                ModLoadingWarning warning = new ModLoadingWarning(
                    modInfo, stage, i18nMessage,
                    exception.getMessage(),
                    exception.getClass().getSimpleName()
                );

                ModLoader.get().addWarning(warning);

                TwitchSpawn.LOGGER.error(exception.getMessage());
            }
        });
    }
}
