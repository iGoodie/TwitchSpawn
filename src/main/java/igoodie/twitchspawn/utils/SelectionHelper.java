package igoodie.twitchspawn.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class SelectionHelper {
	
	public static String selectReward(JsonArray rewards, double amount) {
		for(int i=rewards.size()-1; i>=0; i--) {
			// Traverse all the rewards in reverse order, to find max
			JsonObject reward = rewards.get(i).getAsJsonObject();
			double minAmount = reward.get("minimum_amount").getAsDouble();
			
			if(amount >= minAmount) {
				JsonArray rewardSet = reward.get("items").getAsJsonArray();
				return rewardSet.get(Randomizer.randomInt(0, rewardSet.size()-1)).getAsString();
			}
		}
		
		return null;
	}
	
}
