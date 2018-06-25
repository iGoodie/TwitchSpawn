package igoodie.twitchspawn.packet;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import igoodie.twitchspawn.TSConstants;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class TwitchSpawnPacketHandler implements TSConstants {
	
	public static final SimpleNetworkWrapper NETWORK_WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);
	
	public static final Charset UTF8 = StandardCharsets.UTF_8;
	
	public static void init() {
		NETWORK_WRAPPER.registerMessage(PacketDisplayTitle.class, PacketDisplayTitle.Message.class, 0, Side.CLIENT); //0 : Display Title & Subtitle Command
	}
}
