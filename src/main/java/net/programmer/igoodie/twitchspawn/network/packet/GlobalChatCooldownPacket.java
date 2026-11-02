package net.programmer.igoodie.twitchspawn.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.programmer.igoodie.twitchspawn.client.gui.GlobalChatCooldownOverlay;
import net.programmer.igoodie.twitchspawn.network.NetworkManager;

public record GlobalChatCooldownPacket(long timestamp) implements CustomPacketPayload {

    public static final Type<GlobalChatCooldownPacket> TYPE = NetworkManager.type("global_chat_cooldown");

    public static final StreamCodec<ByteBuf, GlobalChatCooldownPacket> CODEC =
            ByteBufCodecs.LONG.map(GlobalChatCooldownPacket::new, GlobalChatCooldownPacket::timestamp);

    @Override
    public Type<GlobalChatCooldownPacket> type() {
        return TYPE;
    }

    public static void handle(final GlobalChatCooldownPacket packet) {
        GlobalChatCooldownOverlay.setCooldownTimestamp(packet.timestamp);
    }

}
