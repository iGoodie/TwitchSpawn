package net.programmer.igoodie.twitchspawn.tslanguage.predicate;

public class PrefixComparator extends TSLComparator {

    private String prefix;

    public PrefixComparator(String rightHandRaw) {
        this.prefix = rightHandRaw;
    }

    @Override
    public boolean compare(Object leftHand) {
        return (leftHand instanceof String)
                && ((String) leftHand).toLowerCase().trim()
                .startsWith(prefix.toLowerCase().trim());
    }

}
