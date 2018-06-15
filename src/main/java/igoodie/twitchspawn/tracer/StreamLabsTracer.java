package igoodie.twitchspawn.tracer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.PriorityQueue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import igoodie.twitchspawn.configs.Configs;
import igoodie.twitchspawn.model.Donation;
import igoodie.twitchspawn.utils.FileUtils;
import igoodie.twitchspawn.utils.MinecraftServerUtils;
import igoodie.twitchspawn.utils.Randomizer;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;

/**
 * Effective Side = Server </br>
 * Trackers are always going to be @ Effective Server Side
 */
public class StreamLabsTracer extends Thread {
	public static final String DONATIONS_REQ_URL_PATTERN = "https://streamlabs.com/api/donations?access_token=${access_token}&limit=1";
	public final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static StreamLabsTracer instance;

	public static String formedUrl;

	private volatile int tracker = 0;
	private volatile boolean running = false;
	
	private volatile HashSet<String> invalidDonations = new HashSet<>();
	public volatile PriorityQueue<Donation> donationQueue = new PriorityQueue<>();
	
	/* Singleton model & static methods */
	private StreamLabsTracer() {}
	
	public static void init(ICommandSender sender) {
		//Fetch the token from configs and form the URL
		String legacyToken = Configs.json.get("access_token").getAsString();
		formedUrl = DONATIONS_REQ_URL_PATTERN.replace("${access_token}", legacyToken);
		
		//Pre-check if configs are proper
		if(legacyToken.isEmpty() || legacyToken==null) {
			FMLLog.log.error("TwitchSpawn won't work, because no valid legacy token found in the configs");
			MinecraftServerUtils.noticeChatFor(sender, "TwitchSpawn configs are invalid. Please check/refill them", TextFormatting.RED);
			return;
		}
		
		//Initiate the instance & start thread
		instance = new StreamLabsTracer();
		instance.setName("Streamlabs Tracer");
		instance.start();
	}
	
	public static boolean isRunning() {
		return instance!=null && instance.running;
	}
	
	public static void stopRunning(ICommandSender sender) {
		instance.running = false;
		instance.donationQueue.clear();
		MinecraftServerUtils.noticeChatFor(sender, "TwitchSpawn stopped.", TextFormatting.AQUA);
	}
	
	public static int getTrackedTime() {
		return instance==null ? 0 : instance.tracker;
	}
	
	/* Thread insider methods */
	@Override
	public void run() {
		//Initial run. Fetch donations and count them all as displayed.
		JsonArray donations = FileUtils.fetchJson(formedUrl).get("donations").getAsJsonArray();
		for(JsonElement j : donations) {
			instance.invalidDonations.add(j.getAsJsonObject().get("id").getAsString());
		}
		String streamerNick = Configs.json.get("streamer_mc_nick").getAsString();
		MinecraftServerUtils.noticeChatFor(streamerNick, "TwitchSpawn is ready to spawn items!", TextFormatting.AQUA);
		running = true;
		
		//Begin tracing
		while(running) {
			//Fetch json & check if successfully fetched
			JsonObject json = FileUtils.fetchJson(formedUrl);
			if(json==null) continue;
			tracker++;
			
			//Get donations from json
			donations = json.get("donations").getAsJsonArray();
			
			//Traverse donation
			traverseDonations(donations);
			
			//Handle donation if there's any queued
			if(!donationQueue.isEmpty()) {
				handleDonation(donationQueue.poll());
			}
			
			//System.out.println(Minecraft.getMinecraft().world);
			freeze(6); //Check per 6 secs for now
		}
	}
	
	public void traverseDonations(JsonArray donations) {
		for(JsonElement j : donations) {
			try {
				//Parse donation id, and check if that displayed before
				String donationId = j.getAsJsonObject().get("id").getAsString();
				if(!invalidDonations.contains(donationId)) {
					Donation d = new Donation();
					d.amount = j.getAsJsonObject().get("amount").getAsDouble();
					d.username = j.getAsJsonObject().get("donator").getAsJsonObject().get("name").getAsString();
					d.timestamp = SIMPLE_DATE_FORMAT.parse(j.getAsJsonObject().get("created_at").getAsString()).getTime();
					d.note = j.getAsJsonObject().get("message").getAsString();
					
					donationQueue.add(d);
					invalidDonations.add(donationId);
				}
			}
			catch(ParseException e) { e.printStackTrace(); }
		}
	}

	public void handleDonation(Donation newDonation) {
		JsonArray rewards = Configs.json.get("rewards").getAsJsonArray();
		
		//Traverse all the rewards in reverse order
		for(int i=rewards.size()-1; i>=0; i--) {
			JsonObject rewardObject = rewards.get(i).getAsJsonObject();
			double minCur = rewardObject.get("minimum_currency").getAsDouble();
			
			//We found a reward match
			if(newDonation.amount >= minCur) {
				//Fetch streamer nick and matching reward set to pick random reward
				String streamerNick = Configs.json.get("streamer_mc_nick").getAsString();
				JsonArray rewardSet = rewardObject.get("blocks").getAsJsonArray();
				
				//If no reward specified, continue searching
				if(rewardSet.size() == 0) continue;
				
				//Fetch a random reward id (e.g. minecraft:stick)
				String rewardId = rewardSet.get(Randomizer.randomInt(0, rewardSet.size()-1)).getAsString();
				
				//Fetch player in existing world
				MinecraftServer mcs = FMLCommonHandler.instance().getMinecraftServerInstance();
				EntityPlayerMP player = mcs.getPlayerList().getPlayerByUsername(streamerNick);
				
				//Create the item stack with generated item id, and rename it with donator's name
				ItemStack itemstack = new ItemStack(Item.getByNameOrId(rewardId), 1).setStackDisplayName(newDonation.username);
				
				/*TODO: NBT JSON Configs
				try {
					itemstack = new ItemStack(Items.DIAMOND_PICKAXE);
					itemstack.setTagCompound(JsonToNBT.getTagFromJson("{display:{Name:\"Sa\", Lore:\"Something\"}, Unbreakable:1}"));
				} 
				catch (NBTException e) { MinecraftUtils.noticeChat(player, e.getMessage()); } */
				
				//Drop item in front of streamer
				player.dropItem(itemstack, false);
				
				//As the server, send given player Title and Subtitle data to notice on screen
				MinecraftServerUtils.noticeScreen(player, newDonation.username + " donated!", newDonation.username + " rewarded you with %s|" + itemstack.getItem().getUnlocalizedName(itemstack));
				return;
			}
		}
	}
	
	protected void freeze(int timeSec) {
		try {
			synchronized (this) { 
				this.wait(timeSec * 1000); 
			}
		}
		catch (Exception e) {}
	}
}
