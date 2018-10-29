package igoodie.twitchspawn.tracer;

import java.net.URISyntaxException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import igoodie.twitchspawn.TwitchSpawn;
import igoodie.twitchspawn.configs.Configs;
import igoodie.twitchspawn.model.Donation;
import igoodie.twitchspawn.utils.JsonHelper;
import igoodie.twitchspawn.utils.MinecraftServerUtils;
import igoodie.twitchspawn.utils.Randomizer;
import igoodie.twitchspawn.utils.SelectionHelper;
import io.socket.client.IO;
import io.socket.client.IO.Options;
import io.socket.client.Socket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class StreamLabsSocket {

	public static final String SOCKET_URI_FORMAT = "https://sockets.streamlabs.com?token=${socketToken}";
	
	public static StreamLabsSocket instance;
	
	public static boolean isRunning() {
		return instance != null;
	}
	
	public static void start(String socketToken) {
		if(instance != null)
			throw new IllegalStateException("Streamlabs Socket already started!");
		
		if(socketToken==null || socketToken.isEmpty())
			throw new IllegalArgumentException("Invalid socket token.");
		
		Options opts = new Options();
		opts.forceNew = true;
		opts.reconnection = false;
		opts.transports = new String[]{"websocket"};
		
		try {
			instance = new StreamLabsSocket(socketToken, opts);
		} catch(InternalError e) {
			instance = null;
			throw e;
		}
	}
	
	public static void dispose() {
		if(instance==null)
			throw new IllegalStateException("Streamlabs Socket is already disposed!");
		
		instance.socket.disconnect();
		instance = null;
	}
	
	public Socket socket;
	
	private StreamLabsSocket(String socketToken, Options opts) {
		try {
			String socketUri = SOCKET_URI_FORMAT.replace("${socketToken}", socketToken);
			TwitchSpawn.LOGGER.info("Trying to connect socket: " + socketUri);
			socket = IO.socket(socketUri);
		} catch (URISyntaxException e) { // Invalid URI, We're outdated, streamlabs changed the URI!
			throw new InternalError();
		}
		
		socket.on(Socket.EVENT_CONNECT, (obj) -> {
			TwitchSpawn.LOGGER.info("Socket connection success!");
		});
		
		socket.on(Socket.EVENT_DISCONNECT, (obj) -> {
			TwitchSpawn.LOGGER.info("Socket disconnected..");
			MinecraftServerUtils.noticeChatAll("Streamlabs Socket disconnected.. This could be caused by invalid socket token."
					+ " Please double check your token.", TextFormatting.RED);
			dispose();
		});
		
		socket.on("event", (args) -> {
			JSONObject obj = (JSONObject) args[0];
			String obj_type = JsonHelper.extractString(obj, "type");
			String obj_for = JsonHelper.extractString(obj, "for");
			JSONArray message = JsonHelper.extractJSONArray(obj, "message");
			TwitchSpawn.LOGGER.info("Received: " + obj_type + " | " + obj_for);
			
			switch(obj_type + "|" + obj_for) {
			case "donation|streamlabs": handleDonation(message); break;
			}
			
		});

		socket.connect();
	}
	
	private void handleDonation(JSONArray donationMessage) {
		JsonObject rewards = Configs.json.get("rewards").getAsJsonObject();
		JsonArray donationRewards = rewards.get("donation_rewards").getAsJsonArray();
		
		JsonHelper.forEachJSONObject(donationMessage, (donation)->{
			// Traverse all the rewards in reverse order, to find max
			double amount = JsonHelper.extractDouble(donation, "amount");
			String selectedReward = SelectionHelper.selectReward(donationRewards, amount);
			TwitchSpawn.LOGGER.info("Selected: " + selectedReward);
		});

//		//Traverse all the rewards in reverse order
//		for(int i=rewards.size()-1; i>=0; i--) {
//			JsonObject rewardObject = rewards.get(i).getAsJsonObject();
//			double minCur = rewardObject.get("minimum_currency").getAsDouble();
//
//			//We found a reward match
//			if(newDonation.amount >= minCur) {
//				//Fetch streamer nick and matching reward set to pick random reward
//				String streamerNick = Configs.json.get("streamer_mc_nick").getAsString();
//				JsonArray rewardSet = rewardObject.get("blocks").getAsJsonArray();
//
//				//If no reward specified, continue searching
//				if(rewardSet.size() == 0) continue;
//
//				//Fetch a random reward id (e.g. minecraft:stick)
//				String rewardId = rewardSet.get(Randomizer.randomInt(0, rewardSet.size()-1)).getAsString();
//
//				//Fetch player in existing world
//				MinecraftServer mcs = FMLCommonHandler.instance().getMinecraftServerInstance();
//				EntityPlayerMP player = mcs.getPlayerList().getPlayerByUsername(streamerNick);
//
//				//Create the item stack with generated item id, and rename it with donator's name
//				ItemStack itemstack = new ItemStack(Item.getByNameOrId(rewardId), 1).setStackDisplayName(newDonation.username);
//
//				/*TODO: NBT JSON Configs
//				try {
//					itemstack = new ItemStack(Items.DIAMOND_PICKAXE);
//					itemstack.setTagCompound(JsonToNBT.getTagFromJson("{display:{Name:\"Sa\", Lore:\"Something\"}, Unbreakable:1}"));
//				} 
//				catch (NBTException e) { MinecraftUtils.noticeChat(player, e.getMessage()); } */
//
//				//Drop item in front of streamer
//				player.dropItem(itemstack, false);
//
//				//As the server, send given player Title and Subtitle data to notice on screen
//				MinecraftServerUtils.noticeScreen(player, newDonation.username + " donated!", newDonation.username + " rewarded you with %s|" + itemstack.getItem().getUnlocalizedName(itemstack));
//				return;
//			}
//		}
	}
}
