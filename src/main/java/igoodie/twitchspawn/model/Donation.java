package igoodie.twitchspawn.model;

public class Donation implements Comparable<Donation> {
	public String username;
	public double amount;
	public long timestamp;
	public String note;
	
	@Override
	public int compareTo(Donation o) {
		if(timestamp > o.timestamp) return 1;
		if(timestamp < o.timestamp) return -1;
		return 0;
	}
}
