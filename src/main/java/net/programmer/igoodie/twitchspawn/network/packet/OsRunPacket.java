package net.programmer.igoodie.twitchspawn.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.programmer.igoodie.twitchspawn.network.NetworkManager;
import net.programmer.igoodie.twitchspawn.tslanguage.action.OsRunAction;

public record OsRunPacket(OsRunAction.Shell shell, String script) implements CustomPacketPayload {

    public static final Type<OsRunPacket> TYPE = NetworkManager.type("os_run");

    public static final StreamCodec<ByteBuf, OsRunPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(i -> OsRunAction.Shell.values()[i], Enum::ordinal), OsRunPacket::shell,
            ByteBufCodecs.STRING_UTF8, OsRunPacket::script,
            OsRunPacket::new);

    @Override
    public Type<OsRunPacket> type() {
        return TYPE;
    }

    public static void handle(final OsRunPacket packet) {
        OsRunAction.handleLocalScript(packet.shell, packet.script);
    }

}
