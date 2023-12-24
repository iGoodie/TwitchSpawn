package net.programmer.igoodie.twitchspawn.registries;


import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;


/**
 * This class registers sound events.
 */
public class TwitchSpawnSoundEvent
{
    /**
     * Registry for sound events.
     */
    public static final DeferredRegister<SoundEvent> REGISTRY =
        DeferredRegister.create(TwitchSpawn.MOD_ID, Registries.SOUND_EVENT);

    /**
     * Sound event for popping in.
     */
    public static final RegistrySupplier<SoundEvent> POP_IN = REGISTRY.register("pop_in",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(TwitchSpawn.MOD_ID)));

    /**
     * Sound event for popping out.
     */
    public static final RegistrySupplier<SoundEvent> POP_OUT = REGISTRY.register("pop_out",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(TwitchSpawn.MOD_ID)));
}
