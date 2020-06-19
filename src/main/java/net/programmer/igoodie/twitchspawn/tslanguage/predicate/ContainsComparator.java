package net.programmer.igoodie.twitchspawn.tslanguage.predicate;

import java.util.Set;

public class ContainsComparator extends TSLComparator {

    protected String value;

    public ContainsComparator(String rightHandRaw) {
        this.value = rightHandRaw.trim();
    }

    @Override
    public boolean compare(Object leftHand) {
        System.out.println(leftHand.toString() + " contains? " + value.toLowerCase());
        if (leftHand instanceof Set)
            return ((Set) leftHand).contains(value.toLowerCase());

        return false;
    }

}
