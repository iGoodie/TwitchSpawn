package net.programmer.igoodie.twitchspawn.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.programmer.igoodie.twitchspawn.tslanguage.action.OsRunAction;

import java.nio.charset.StandardCharsets;

public class OsRunPacket implements IMessageHandler<OsRunPacket.Message, IMessage> {

    public static class Message implements IMessage {

        private OsRunAction.Shell shell;
        private String script;

        public Message(OsRunAction.Shell shell, String script) {
            this.shell = shell;
            this.script = script;
        }

        @Override
        public void toBytes(ByteBuf buffer) {
            buffer.writeInt(this.shell.ordinal());
            buffer.writeInt(this.script.length());
            buffer.writeCharSequence(this.script, StandardCharsets.UTF_8);
        }

        @Override
        public void fromBytes(ByteBuf buffer) {
            this.shell = OsRunAction.Shell.values()[buffer.readInt()];
            this.script = buffer.readCharSequence(buffer.readInt(), StandardCharsets.UTF_8).toString();
        }

    }

    @Override
    public IMessage onMessage(Message message, MessageContext ctx) {
        OsRunAction.handleLocalScript(message.shell, message.script);

        return null; // No response
    }

}
