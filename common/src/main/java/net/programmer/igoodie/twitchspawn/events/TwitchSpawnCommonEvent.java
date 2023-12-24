package net.programmer.igoodie.twitchspawn.events;


import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.programmer.igoodie.twitchspawn.TwitchSpawnLoadingErrors;


public interface TwitchSpawnCommonEvent
{
    Event<SetupEvent> SETUP_EVENT = EventFactory.createCompoundEventResult();

    interface SetupEvent
    {
        void setupEvent(TwitchSpawnLoadingErrors error);
    }
}
