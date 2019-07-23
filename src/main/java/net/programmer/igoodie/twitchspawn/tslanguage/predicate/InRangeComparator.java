package net.programmer.igoodie.twitchspawn.tslanguage.predicate;

import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InRangeComparator extends TSLComparator {

    public static final String SYMBOL = "IN RANGE";

    double min, max;

    /**
     * Constructs a range comparator
     *
     * @param rightHandRaw Raw right hand script. (E.g [0.20])
     */
    public InRangeComparator(String rightHandRaw) throws TSLSyntaxError {
        Pattern pattern = Pattern.compile("^\\[(.+)\\,(.+)\\]$");
        Matcher matcher = pattern.matcher(rightHandRaw);

        if (!matcher.find())
            throw new TSLSyntaxError("Expected format like [1.0,2.0], found -> " + rightHandRaw);

        try {
            min = Double.parseDouble(matcher.group(1));
            max = Double.parseDouble(matcher.group(2));

        } catch (NumberFormatException e) {
            throw new TSLSyntaxError("Expected valid numbers, found -> "
                    + matcher.group(1) + " and " + matcher.group(2));
        }

        if (min > max)
            throw new TSLSyntaxError("Expected first value to be less than the second value.");
    }

    @Override
    public boolean compare(Object leftHand) {
        if (!(leftHand instanceof Number))
            return false;

        double number = (Double) leftHand;

        return min <= number && number <= max;
    }

}
