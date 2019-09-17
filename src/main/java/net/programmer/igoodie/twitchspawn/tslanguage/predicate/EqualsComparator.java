package net.programmer.igoodie.twitchspawn.tslanguage.predicate;

import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

public class EqualsComparator extends BasicComparator {

    public EqualsComparator(String rightHandRaw) throws TSLSyntaxError {
        super(rightHandRaw);
    }

    @Override
    public boolean compare(Object leftHand) {
        if (leftHand instanceof Number)
            return ((Number) leftHand).doubleValue() == value;

        return leftHand.equals(value);
    }

}
