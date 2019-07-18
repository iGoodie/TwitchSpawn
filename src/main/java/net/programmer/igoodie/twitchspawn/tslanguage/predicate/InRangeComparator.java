package net.programmer.igoodie.twitchspawn.tslanguage.predicate;

import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

public class InRangeComparator extends TSLComparator {

    public static final String SYMBOL = "IN RANGE";

    int min, max;

    /**
     * Constructs a range comparator
     * @param rightHandRaw Raw right hand script. (E.g [0.20])
     */
    public InRangeComparator(String rightHandRaw) throws TSLSyntaxError {
        System.out.println("Parsing InRangeComparator for " + rightHandRaw);


    }

    @Override
    public boolean compare(Object leftHand) {
        return false;
    }

}
