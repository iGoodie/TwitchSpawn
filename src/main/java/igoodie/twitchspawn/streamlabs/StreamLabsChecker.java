package igoodie.twitchspawn.streamlabs;

import igoodie.twitchspawn.configs.Configs;
import igoodie.twitchspawn.model.Donation;
import igoodie.twitchspawn.utils.FileUtils;
import igoodie.twitchspawn.utils.MinecraftUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;

import net.minecraft.client.resources.I18n;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class StreamLabsChecker extends Thread {
	public static final String DONATIONS_REQ_URL_PATTERN = "https://streamlabs.com/api/donations?access_token=%s&limit=1";
	public final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	//TODO: impl queue

	public static String formedUrl;
	public static StreamLabsChecker instance;
	public static long lastDonationTime = -1; //-1 if not loaded yet
	public static Random rand = new Random(System.currentTimeMillis());

	int tracker = 0;
	private volatile boolean running = false;
	private volatile HashSet<String> queuedDonations = new HashSet<>();
	public volatile PriorityQueue<Donation> donationQueue = new PriorityQueue<>();
	
	/* Singleton model */
	private StreamLabsChecker() { running = true; }
	
	public static void init(ICommandSender sender) {
		//Pre-check if already running
		if(isRunning()) {
			MinecraftUtils.noticeChat(sender, "TwitchSpawn is already running.", TextFormatting.RED);
			return;
		}
		
		//Fetch the token from configs and form the URL
		String legacyToken = Configs.json.get("access_token").getAsString();
		formedUrl = String.format(DONATIONS_REQ_URL_PATTERN, legacyToken);
		
		//Pre-check if configs are proper
		if(legacyToken.isEmpty() || legacyToken==null || FileUtils.fetchJson(formedUrl)==null) {
			FMLLog.log.error("TwitchSpawn won't work, because no valid legacy token found in the configs");
			MinecraftUtils.noticeChat(sender, "TwitchSpawn configs are invalid. Please check/refill them", TextFormatting.RED);
			return;
		}
		
		//Initiate the instance
		instance = new StreamLabsChecker();
		
		//Fetch first donations and detect last donation time
		JsonArray donations = FileUtils.fetchJson(formedUrl).get("donations").getAsJsonArray();
		for(JsonElement j : donations) {
			try {
				String creation = j.getAsJsonObject().get("created_at").getAsString();
				long creationUnix = SIMPLE_DATE_FORMAT.parse(creation).getTime();
				instance.queuedDonations.add(j.getAsJsonObject().get("id").getAsString());
				lastDonationTime = Math.max(creationUnix, lastDonationTime);
			}
			catch(ParseException e) { e.printStackTrace(); }
		}
		System.out.println("Detected last donation: "+lastDonationTime);
		
		//Start thread
		instance.start();

		//Notice it for the runner
		MinecraftUtils.noticeChat(sender, "TwitchSpawn is ready to spawn items!", TextFormatting.BLUE);
	}
	
	public static boolean isRunning() {
		return instance!=null && instance.running;
	}
	
	public static void stopRunning(ICommandSender sender) {
		if(instance==null || !instance.running) {			
			MinecraftUtils.noticeChat(sender, "TwitchSpawn isn't running.", TextFormatting.RED);
			return;
		}
		
		instance.running = false;
		instance.donationQueue.clear();
		MinecraftUtils.noticeChat(sender, "TwitchSpawn stopped.", TextFormatting.BLUE);
	}
	
	@Override
	public void run() {
		while(running) {
			//Fetch json & check if successfully fetched
			JsonObject json = FileUtils.fetchJson(formedUrl);
			if(json==null) continue;
			tracker++;
			
			//Get donations from json
			JsonArray donations = json.get("donations").getAsJsonArray();
			
			//Traverse donation
			traverseDonations(donations);
			
			//Handle donation if there's any queued
			if(!donationQueue.isEmpty()) {
				handleDonation(donationQueue. poll());
			}
			
			//System.out.println(Minecraft.getMinecraft().world);
			freeze(6); //Check per 6 secs for now
		}
	}
	
	public void traverseDonations(JsonArray donations) {
		for(JsonElement j : donations) {
			try {
				String creation = j.getAsJsonObject().get("created_at").getAsString();
				long creationUnix = SIMPLE_DATE_FORMAT.parse(creation).getTime();
				String donationId = j.getAsJsonObject().get("id").getAsString();
				//New donation found!
				if(creationUnix >= lastDonationTime && !queuedDonations.contains(donationId)) {
					Donation d = new Donation();
					d.amount = j.getAsJsonObject().get("amount").getAsDouble();
					d.username = j.getAsJsonObject().get("donator").getAsJsonObject().get("name").getAsString();
					d.timestamp = creationUnix;
					d.note = j.getAsJsonObject().get("message").getAsString();
					
					System.out.println(creation + "|" + creationUnix);
					
					donationQueue.add(d);
					queuedDonations.add(donationId);
					lastDonationTime = creationUnix;
				}
			}
			catch(ParseException e) { e.printStackTrace(); }
		}
	}

	public void handleDonation(Donation d) {
		JsonArray rewards = Configs.json.get("rewards").getAsJsonArray();
		for(int i=rewards.size()-1; i>=0; i--) {
			JsonObject rewardObject = rewards.get(i).getAsJsonObject();
			double minCur = rewardObject.get("minimum_currency").getAsDouble();
			if(d.amount >= minCur) {
				String streamerNick = Configs.json.get("streamer_nick").getAsString();
				JsonArray rewardSet = rewardObject.get("blocks").getAsJsonArray();
				String rewardId = rewardSet.get(rand.nextInt(rewardSet.size())).getAsString();
				
				MinecraftServer mcs = FMLCommonHandler.instance().getMinecraftServerInstance();
				EntityPlayerMP player = mcs.getPlayerList().getPlayerByUsername(streamerNick);
				ItemStack itemstack = new ItemStack(Item.getByNameOrId(rewardId), 1).setStackDisplayName(d.username);
				player.dropItem(itemstack, false);
				String localizedName = I18n.format(itemstack.getItem().getUnlocalizedName(itemstack)+".name");
				MinecraftUtils.noticeScreen(d.username+" donated!", d.username+" rewarded you with "+localizedName);
				return;
			}
		}
	}
	
	protected void freeze(int timeSec) {
		try {
			synchronized (this) {
				//Freeze for given secs
				this.wait(timeSec * 1000);
			}
		}
		catch (Exception e) {}
	}
}
