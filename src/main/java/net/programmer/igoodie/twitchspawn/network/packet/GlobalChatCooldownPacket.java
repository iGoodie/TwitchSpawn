package net.programmer.igoodie.twitchspawn.network.packet;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.programmer.igoodie.twitchspawn.client.gui.GlobalChatCooldownOverlay;

import java.util.function.Supplier;

public class GlobalChatCooldownPacket {

    public static void encode(GlobalChatCooldownPacket packet, PacketBuffer buffer) {
        buffer.writeLong(packet.timestamp);
    }

    public static GlobalChatCooldownPacket decode(PacketBuffer buffer) {
        return new GlobalChatCooldownPacket(buffer.readLong());
    }

    public static void handle(final GlobalChatCooldownPacket packet,
                              Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            GlobalChatCooldownOverlay.setCooldownTimestamp(packet.timestamp);
        });
        context.get().setPacketHandled(true);
    }

    /* -------------------------------- */

    private long timestamp;

    public GlobalChatCooldownPacket(long timestamp) {
        this.timestamp = timestamp;
    }

}
