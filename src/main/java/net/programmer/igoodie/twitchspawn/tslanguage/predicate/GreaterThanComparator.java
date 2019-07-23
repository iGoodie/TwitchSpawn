package net.programmer.igoodie.twitchspawn.tslanguage.predicate;

import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

public class GreaterThanComparator extends TSLComparator {

    public static final String SYMBOL = ">";

    public double value;

    public GreaterThanComparator(String rightHandRaw) throws TSLSyntaxError {
        try {
            this.value = Double.parseDouble(rightHandRaw);

        } catch (NumberFormatException e) {
            throw new TSLSyntaxError("Expected a valid fractional number, found " + rightHandRaw + " instead.");
        }
    }

    @Override
    public boolean compare(Object leftHand) {
        if(!(leftHand instanceof Number))
            return false;

        return ((Double)leftHand) > value;
    }

}
