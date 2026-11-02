package net.programmer.igoodie.twitchspawn;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.programmer.igoodie.twitchspawn.network.packet.GlobalChatCooldownPacket;
import net.programmer.igoodie.twitchspawn.network.packet.OsRunPacket;
import net.programmer.igoodie.twitchspawn.network.packet.StatusChangedPacket;
import net.programmer.igoodie.twitchspawn.tslanguage.action.OsRunAction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PacketTests {

    /**
     * Encodes and decodes the packet, expecting an equal packet,
     * the decoder to consume the whole buffer and re-encoding to give identical bytes.
     */
    private static <T extends CustomPacketPayload> void assertRoundTrip(T packet, StreamCodec<ByteBuf, T> codec) {
        ByteBuf buffer = Unpooled.buffer();
        codec.encode(buffer, packet);
        byte[] originalBytes = ByteBufUtil.getBytes(buffer);

        T decoded = codec.decode(buffer);
        Assertions.assertEquals(0, buffer.readableBytes(), "Decoder left unread bytes");
        Assertions.assertEquals(packet, decoded);
        Assertions.assertEquals(packet.type(), decoded.type());

        ByteBuf reencoded = Unpooled.buffer();
        codec.encode(reencoded, decoded);
        Assertions.assertArrayEquals(originalBytes, ByteBufUtil.getBytes(reencoded));
    }

    @Test
    @DisplayName("should round-trip StatusChangedPacket.")
    public void statusChangedTest() {
        assertRoundTrip(new StatusChangedPacket(true), StatusChangedPacket.CODEC);
        assertRoundTrip(new StatusChangedPacket(false), StatusChangedPacket.CODEC);
    }

    @Test
    @DisplayName("should round-trip GlobalChatCooldownPacket.")
    public void globalChatCooldownTest() {
        assertRoundTrip(new GlobalChatCooldownPacket(1_700_000_000_000L), GlobalChatCooldownPacket.CODEC);
        assertRoundTrip(new GlobalChatCooldownPacket(0L), GlobalChatCooldownPacket.CODEC);
    }

    @Test
    @DisplayName("should round-trip OsRunPacket for every shell.")
    public void osRunTest() {
        for (OsRunAction.Shell shell : OsRunAction.Shell.values()) {
            assertRoundTrip(new OsRunPacket(shell, "echo \"héllo wörld\" && exit 0"), OsRunPacket.CODEC);
        }
    }

    @Test
    @DisplayName("should use distinct payload ids under the mod namespace.")
    public void payloadIdsTest() {
        Set<Identifier> ids = Stream.of(
                StatusChangedPacket.TYPE,
                OsRunPacket.TYPE,
                GlobalChatCooldownPacket.TYPE
        ).map(CustomPacketPayload.Type::id).collect(Collectors.toSet());

        Assertions.assertEquals(3, ids.size());
        ids.forEach(id -> Assertions.assertEquals(TwitchSpawn.MOD_ID, id.getNamespace()));
    }

}
