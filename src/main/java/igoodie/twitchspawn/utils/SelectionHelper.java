package igoodie.twitchspawn.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.util.StringUtils;

public class SelectionHelper {
	
	public static String selectDonationReward(JsonArray rewards, double amount) {
		return selectReward(rewards, "minimum_amount", amount);
	}
	
	public static String selectBitReward(JsonArray rewards, double amount) {
		return selectReward(rewards, "minimum_bit", amount);
	}
	
	public static String selectSubsReward(JsonArray rewards, double amount) {
		return selectReward(rewards, "minimum_months", amount);
	}
	
	/**
	 * <b>Requires rewards array to be sorted!</b> <br>
	 * Starting from the last reward, tests availability of the amount.
	 * If no reward is available returns null.
	 * @param rewards Rewards array containing the rewards to be tested.
	 * @param minimumKey Name of the minimum amount field in each reward.
	 * @param amount Amount to be tested with.
	 * @return Capable reward string or null
	 */
	public static String selectReward(JsonArray rewards, String minimumKey, double amount) {
		if(rewards.size() == 0) return null;
		
		if(StringUtils.isNullOrEmpty(minimumKey)) {
			return rewards.get(Randomizer.randomInt(0, rewards.size()-1)).getAsString();
		}
		
		for(int i=rewards.size()-1; i>=0; i--) {
			// Traverse all the rewards in reverse order, to find max
			JsonObject reward = rewards.get(i).getAsJsonObject();
			double minAmount = reward.get(minimumKey).getAsDouble();
			
			// Test if amount is capable
			if(amount >= minAmount) {
				JsonArray rewardSet = reward.get("items").getAsJsonArray();
				return rewardSet.get(Randomizer.randomInt(0, rewardSet.size()-1)).getAsString();
			}
		}
		
		return null;
	}
	
}
