package net.programmer.igoodie.twitchspawn.tslanguage.predicate;

public class PostfixComparator extends TSLComparator {

    private String postfix;

    public PostfixComparator(String rightHandRaw) {
        this.postfix = rightHandRaw;
    }

    @Override
    public boolean compare(Object leftHand) {
        return (leftHand instanceof String)
                && ((String) leftHand).toLowerCase().trim()
                .endsWith(postfix.toLowerCase().trim());
    }

}
