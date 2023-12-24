package net.programmer.igoodie.twitchspawn.tslanguage.predicate;

import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

public class GreaterThanComparator extends BasicComparator {

    public GreaterThanComparator(String rightHandRaw) throws TSLSyntaxError {
        super(rightHandRaw);
    }

    @Override
    public boolean compare(Object leftHand) {
        if (!(leftHand instanceof Number))
            return false;

        return ((Number) leftHand).doubleValue() > value;
    }

}
