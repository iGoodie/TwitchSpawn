package net.programmer.igoodie.twitchspawn.tslanguage.predicate;

import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

public class GreaterThanComparator extends TSLComparator {

    public static final String SYMBOL = ">";

    public GreaterThanComparator(String rightHandRaw) throws TSLSyntaxError {
        System.out.println("Parsing GreaterThanComparator for " + rightHandRaw);


    }

    @Override
    public boolean compare(Object leftHand) {
        return false;
    }

}
