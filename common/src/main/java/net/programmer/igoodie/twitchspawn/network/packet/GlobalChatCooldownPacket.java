package net.programmer.igoodie.twitchspawn.network.packet;


import java.util.function.Supplier;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
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

    public void handle(Supplier<NetworkManager.PacketContext> context) {
        context.get().queue(() -> GlobalChatCooldownOverlay.setCooldownTimestamp(this.timestamp));
    }

    /**
     * Timestamp of the cooldown.
     */
    private final long timestamp;
}
