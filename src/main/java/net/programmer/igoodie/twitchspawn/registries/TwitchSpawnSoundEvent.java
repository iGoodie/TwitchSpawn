package net.programmer.igoodie.twitchspawn.registries;


import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.programmer.igoodie.twitchspawn.TwitchSpawn;


/**
 * This class registers sound events.
 */
@Mod.EventBusSubscriber(modid = TwitchSpawn.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TwitchSpawnSoundEvent
{
    /**
     * Registry for sound events.
     */
    public static final DeferredRegister<SoundEvent> REGISTRY =
        DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, TwitchSpawn.MOD_ID);

    /**
     * Sound event for popping in.
     */
    public static final RegistryObject<SoundEvent> POP_IN = REGISTRY.register("pop_in",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(TwitchSpawn.MOD_ID)));

    /**
     * Sound event for popping out.
     */
    public static final RegistryObject<SoundEvent> POP_OUT = REGISTRY.register("pop_out",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(TwitchSpawn.MOD_ID)));
}
