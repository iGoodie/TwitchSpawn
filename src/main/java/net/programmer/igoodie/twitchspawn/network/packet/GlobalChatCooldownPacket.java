package net.programmer.igoodie.twitchspawn.network.packet;


import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.programmer.igoodie.twitchspawn.client.gui.GlobalChatCooldownOverlay;

public class GlobalChatCooldownPacket {

    public GlobalChatCooldownPacket(long timestamp) {
        this.timestamp = timestamp;
    }

    public static void encode(GlobalChatCooldownPacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.timestamp);
    }

    public static GlobalChatCooldownPacket decode(FriendlyByteBuf buffer) {
        return new GlobalChatCooldownPacket(buffer.readLong());
    }

    public void handle(CustomPayloadEvent.Context context) {
        context.enqueueWork(() ->  GlobalChatCooldownOverlay.setCooldownTimestamp(this.timestamp));
        context.setPacketHandled(true);
    }

    /**
     * Timestamp of the cooldown.
     */
    private final long timestamp;
}
