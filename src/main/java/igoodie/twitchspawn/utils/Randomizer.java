package igoodie.twitchspawn.utils;

import java.util.Random;

public class Randomizer {
	private static final Random RANDOM = new Random(System.currentTimeMillis());
	private static final char[] ALPHABET = ("abcdefghijklmnopqrstuvwxyz" + "abcdefghijklmnopqrstuvwxyz".toUpperCase()).toCharArray();
	
	public static String randomString(int length) {
		String word = "";
		for(int i=0; i<length; i++) {
			word += randomChar();
		}
		return word;
	}

	public static char randomChar() {
		return ALPHABET[RANDOM.nextInt(ALPHABET.length)];
	}
	
	public static int randomInt(int min, int max) {
		return RANDOM.nextInt(max+1-min) + min;
	}
	
	public static int randomInt(int max) {
		return RANDOM.nextInt(max);
	}
	
	public static int randomInt() {
		return RANDOM.nextInt();
	}
	
	public static float randomFloat(float min, float max) {
		return RANDOM.nextFloat() * (max-min) + min;
	}

	public static float randomFloat(float max) {
		return RANDOM.nextFloat() * (max);
	}
	
	public static float randomFloat() {
		return RANDOM.nextFloat() * (Float.MAX_VALUE-Float.MIN_VALUE) + Float.MIN_VALUE;
	}
	
	public static double randomDouble(double min, double max) {
		return RANDOM.nextDouble() * (max-min) + min;
	}

	public static double randomDouble(double max) {
		return RANDOM.nextDouble() * (max);
	}
	
	public static double randomDouble() {
		return RANDOM.nextDouble() * (Double.MAX_VALUE-Double.MIN_VALUE) + Double.MIN_VALUE;
	}
}
