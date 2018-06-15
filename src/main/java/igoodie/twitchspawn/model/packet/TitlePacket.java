package igoodie.twitchspawn.model.packet;

import igoodie.twitchspawn.network.Packets;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class TitlePacket implements IMessage {
	public String title, subtitle;
	
	public TitlePacket() {}
	
	public TitlePacket(String title, String subtitle) {
		this.title = title;
		this.subtitle = subtitle;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(title.length()); //n char
		buf.writeInt(subtitle.length());
		
		buf.writeBytes(title.getBytes(Packets.UTF8)); //2n bytes		
		buf.writeBytes(subtitle.getBytes(Packets.UTF8));		
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		byte[] buffer;
		int titleLen = buf.readInt();
		int subtitleLen = buf.readInt();
		
		buffer = new byte[titleLen];
		buf.readBytes(buffer);
		title = new String(buffer, Packets.UTF8);
		
		buffer = new byte[subtitleLen];
		buf.readBytes(buffer);
		subtitle = new String(buffer, Packets.UTF8);
	}
}
