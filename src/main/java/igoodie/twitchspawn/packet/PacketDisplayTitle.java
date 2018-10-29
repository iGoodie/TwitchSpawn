package igoodie.twitchspawn.packet;

import igoodie.twitchspawn.packet.PacketDisplayTitle.Message;
import igoodie.twitchspawn.utils.MinecraftClientUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketDisplayTitle implements IMessageHandler<Message, IMessage> { //<REQ, RES>
	
	@Override
	public IMessage onMessage(Message message, MessageContext ctx) {
		String[] format = message.subtitle.split("\\|");
		System.out.println(format[0] + "|" + format[1]);
		format[0] = String.format(format[0], I18n.format(format[1] + ".name"));
		System.out.println(format[0] + "|" + format[1]);
		
		MinecraftClientUtils.noticeScreen(message.title, format[0]);
		
		return null; // No response
	}
	
	// Store packet encoder/decoder and model prototype with the handler.
	// Inspired by Electroblob77's Wizardry sources (github.com/Electroblob77/Wizardry)
	public static class Message implements IMessage {
		
		public String title, subtitle;
		
		public Message() {}
		
		public Message(String title, String subtitle) {
			this.title = title;
			this.subtitle = subtitle;
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeInt(title.length()); //n char
			buf.writeInt(subtitle.length());
			
			buf.writeBytes(title.getBytes(TwitchSpawnPacketHandler.UTF8)); //2*n bytes		
			buf.writeBytes(subtitle.getBytes(TwitchSpawnPacketHandler.UTF8));		
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			byte[] buffer;
			int titleLen = buf.readInt();
			int subtitleLen = buf.readInt();
			
			buffer = new byte[titleLen];
			buf.readBytes(buffer);
			title = new String(buffer, TwitchSpawnPacketHandler.UTF8);
			
			buffer = new byte[subtitleLen];
			buf.readBytes(buffer);
			subtitle = new String(buffer, TwitchSpawnPacketHandler.UTF8);
		}

	}
}
