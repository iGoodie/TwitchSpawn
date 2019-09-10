package net.programmer.igoodie.twitchspawn.network.packet;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.programmer.igoodie.twitchspawn.tslanguage.action.OsRunAction;

import java.util.function.Supplier;

public class OsRunPacket {

    public static void encode(OsRunPacket packet, PacketBuffer buffer) {
        buffer.writeInt(packet.shell.ordinal());
        buffer.writeString(packet.script);
    }

    public static OsRunPacket decode(PacketBuffer buffer) {
        OsRunAction.Shell shell = OsRunAction.Shell.values()[buffer.readInt()];
        String script = buffer.readString();

        return new OsRunPacket(shell, script);
    }

    public static void handle(final OsRunPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> OsRunAction.handleLocalScript(packet.shell, packet.script));
        context.get().setPacketHandled(true);
    }

    /* ------------------------------------------------ */

    private OsRunAction.Shell shell;
    private String script;

    public OsRunPacket(OsRunAction.Shell shell, String script) {
        this.shell = shell;
        this.script = script;
    }

}
