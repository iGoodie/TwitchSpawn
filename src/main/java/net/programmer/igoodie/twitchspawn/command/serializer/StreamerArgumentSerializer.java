package net.programmer.igoodie.twitchspawn.command.serializer;

import com.google.gson.JsonObject;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.programmer.igoodie.twitchspawn.command.StreamerArgumentType;

public class StreamerArgumentSerializer implements ArgumentSerializer<StreamerArgumentType> {

    @Override
    public void serializeToNetwork(StreamerArgumentType argumentType, FriendlyByteBuf buffer) {

    }

    @Override
    public StreamerArgumentType deserializeFromNetwork(FriendlyByteBuf buffer) {
        return StreamerArgumentType.streamerNick();
    }

    @Override
    public void serializeToJson(StreamerArgumentType argumentType, JsonObject jsonObject) {

    }

}
