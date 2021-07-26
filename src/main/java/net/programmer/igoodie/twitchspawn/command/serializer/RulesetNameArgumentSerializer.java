package net.programmer.igoodie.twitchspawn.command.serializer;

import com.google.gson.JsonObject;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.programmer.igoodie.twitchspawn.command.RulesetNameArgumentType;

public class RulesetNameArgumentSerializer implements ArgumentSerializer<RulesetNameArgumentType> {

    @Override
    public void serializeToNetwork(RulesetNameArgumentType p_121579_, FriendlyByteBuf p_121580_) {

    }

    @Override
    public RulesetNameArgumentType deserializeFromNetwork(FriendlyByteBuf p_121581_) {
        return RulesetNameArgumentType.rulesetName();
    }

    @Override
    public void serializeToJson(RulesetNameArgumentType p_121577_, JsonObject p_121578_) {

    }

}
