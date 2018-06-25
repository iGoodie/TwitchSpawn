package igoodie.twitchspawn.tracer;

import java.util.HashSet;
import java.util.PriorityQueue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import igoodie.twitchspawn.configs.Configs;
import igoodie.twitchspawn.model.Donation;
import igoodie.twitchspawn.utils.MinecraftServerUtils;
import igoodie.twitchspawn.utils.Randomizer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class StreamLabsTracer extends JsonTracer {

	public static final String DONATIONS_REQ_URL_PATTERN = "https://streamlabs.com/api/donations?access_token=${access_token}&limit=1";

	public static StreamLabsTracer instance;
	
	private volatile HashSet<String> checkedDonations = new HashSet<>();
	public volatile PriorityQueue<Donation> unhandledQueue = new PriorityQueue<>();

	public StreamLabsTracer(String name, String accessToken) {
		super(name, DONATIONS_REQ_URL_PATTERN.replace("${access_token}", accessToken), 6);
	}

	public StreamLabsTracer(String accessToken) {
		super("StreamLabs Tracer Thread", DONATIONS_REQ_URL_PATTERN.replace("${access_token}", accessToken), 6);
	}

	@Override
	public void stop() {
		checkedDonations.clear();
		super.stop();
	}
	
	@Override
	protected void preTrace() {
		// Pre-trace and mark previously handled donations
		JsonArray donations = fetch().getAsJsonArray();
		for(JsonElement d : donations) {
			checkedDonations.add(d.getAsJsonObject().get("id").getAsString());
		}
	}

	@Override
	protected void trace(JsonObject fetchedJSON) {
		JsonArray donations = fetchedJSON.get("donations").getAsJsonArray();

		// Traverse donations
		extractUnhandled(donations);

		// Handle next queued donation
		if(!unhandledQueue.isEmpty()) {
			handleDonation(unhandledQueue.poll());
		}
	}

	private void extractUnhandled(JsonArray donations) {
		for(JsonElement d : donations) {
			String donationId = d.getAsJsonObject().get("id").getAsString();
			if(!checkedDonations.contains(donationId)) { // If not handled yet
				checkedDonations.add(donationId);
				unhandledQueue.add(new Donation(d.getAsJsonObject()));
			}
		}
	}

	private void handleDonation(Donation newDonation) {
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
}
