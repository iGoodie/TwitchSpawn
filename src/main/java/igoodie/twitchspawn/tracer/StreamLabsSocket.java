package igoodie.twitchspawn.tracer;

import java.net.URISyntaxException;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import igoodie.twitchspawn.TwitchSpawn;
import igoodie.twitchspawn.configs.Configs;
import igoodie.twitchspawn.utils.JSONHelper;
import igoodie.twitchspawn.utils.MinecraftServerUtils;
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
	public boolean tokenVerified = false;

	private StreamLabsSocket(String socketToken, Options opts) {
		try {
			String socketUri = SOCKET_URI_FORMAT.replace("${socketToken}", socketToken);
			TwitchSpawn.LOGGER.info("Trying to connect socket: " + socketUri);
			socket = IO.socket(socketUri);
		} catch (URISyntaxException e) { // Invalid URI, We're outdated, streamlabs changed the URI!
			throw new InternalError();
		}

		bindHandlers();

		socket.connect();
	}

	private void bindHandlers() {
		socket.on(Socket.EVENT_CONNECT, (obj) -> {
			TwitchSpawn.LOGGER.info("Socket connection success!");
			tokenVerified = true;
		});

		socket.on(Socket.EVENT_DISCONNECT, (obj) -> {
			TwitchSpawn.LOGGER.info("Socket disconnected..");
			if(!tokenVerified) { // If token was not verified via connection				
				MinecraftServerUtils.noticeChatAll("Streamlabs Socket disconnected.. This could be caused by invalid socket token."
						+ " Please double check your token.", TextFormatting.RED);
				instance = null; // Nullify here outside dispose() if it was a force disconnect
			}
		});

		socket.on("event", (args) -> {
			JSONObject obj = (JSONObject) args[0];

			// Parse fields from received packet
			String obj_type = JSONHelper.extractString(obj, "type");
			String obj_for = JSONHelper.extractString(obj, "for");
			JSONArray message = JSONHelper.extractJSONArray(obj, "message");
			TwitchSpawn.LOGGER.info("Received: " + obj_type + " | " + obj_for);

			// Handle that packet accordingly
			handleMessage(obj_type, obj_for, message);
		});
	}

	public void handleMessage(String eventType, String eventFor, JSONArray message) {
		handleMessage(eventType+"|"+eventFor, message);
	}

	public void handleMessage(String eventPair, JSONArray message) {
		TwitchSpawn.LOGGER.info("Handling event pair = " + eventPair);
		switch(eventPair) { // Switch type|for pair
		case "donation|null":		 		handleEvent(message, "donation_rewards", "amount", "minimum_amount", "donation"); break;
		case "donation|streamlabs": 		handleEvent(message, "donation_rewards", "amount", "minimum_amount", "donation"); break;

		case "bits|twitch_account":			handleEvent(message, "bit_rewards", "amount", "minimum_bit", "bit_donation"); break;

		case "subscription|twitch_account": handleEvent(message, "sub_rewards", "months", "minimum_months", "subscription"); break;
		
		case "host|twitch_account": 		handleEvent(message, "host_rewards", "viewers", "minimum_viewer", "host"); break;
		
		case "follow|twitch_account": 		handleEvent(message, "follow_rewards", null, null, "follow"); break;
		}
	}

	public void handleEvent(JSONArray eventMessage, String rewardFieldName, String amountFieldName, String minimumFieldName, String actionName) {
		JsonObject rewards = Configs.configJson.get("rewards").getAsJsonObject();
		JsonArray eventRewards = rewards.get(rewardFieldName).getAsJsonArray();

		// Handle each event received in the message object
		JSONHelper.forEachJSONObject(eventMessage, (donation)->{
			// Select reward with given amount
			double amount = JSONHelper.extractDouble(donation, amountFieldName);
			String selectedReward = SelectionHelper.selectReward(eventRewards, minimumFieldName, amount);

			// If no reward fits that amount, continue to other event messages.
			if(selectedReward == null) return;

			// Fetch server
			MinecraftServer minecraftServer = FMLCommonHandler.instance().getMinecraftServerInstance();

			// Find streamer nick and actor
			String streamerNick = Configs.configJson.get("streamer_mc_nick").getAsString();
			String actorNick = JSONHelper.extractString(donation, "from");
			EntityPlayerMP streamerPlayer = minecraftServer.getPlayerList().getPlayerByUsername(streamerNick);

			// Create item by it's uid and rename the itemstack with actor's nick
			Item item = Item.getByNameOrId(selectedReward);
			ItemStack itemstack = new ItemStack(item, 1).setStackDisplayName(actorNick);

			// Couldn't find the streamer on server, how is it possibru?
			if(streamerPlayer == null) {
				TwitchSpawn.LOGGER.warn("Donation received but streamer could not be found online. Is the config file valid?");
				return;
			}

			// Drop item in front of streamer
			streamerPlayer.dropItem(itemstack, false);

			// Fetch and format upper & lower texts
			String upperText = Configs.customText.get(actionName).getAsJsonObject().get("upper_text_format").getAsString();
			String lowerText = Configs.customText.get(actionName).getAsJsonObject().get("lower_text_format").getAsString();
			upperText = formatText(upperText, actorNick, streamerNick, amount, itemstack);
			lowerText = formatText(lowerText, actorNick, streamerNick, amount, itemstack);

			// As the server, send given player Title and Subtitle data to notice on screen
			MinecraftServerUtils.noticeScreen(streamerPlayer, upperText, lowerText);
		});
	}

	private String formatText(String text, String actorNick, String streamerNick, double amount, ItemStack itemstack) {
		text = text.replace("${actor}", actorNick);
		text = text.replace("${streamer}", streamerNick);

		if(amount != -1) { // If amount exists for this specific format, then replace
			text = text.replace("${amount}", Double.toString(amount));
			text = text.replace("${amount_i}", Integer.toString((int)amount));
		}

		if(text.contains("${item}")) { // If it doesn't contain item tag, do not concat unlocalized name
			text = text.replace("${item}", "%s");
			text = text.concat("|").concat(itemstack.getItem().getUnlocalizedName());
		}
		
		return text; // Return edited text
	}
}
