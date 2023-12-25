
package net.programmer.igoodie.twitchspawn.tslanguage.predicate;

public class IsComparator extends TSLComparator {

    protected String value;

    public IsComparator(String rightHandRaw) {
        this.value = rightHandRaw;
    }

    @Override
    public boolean compare(Object leftHand) {
        if (leftHand instanceof Boolean)
            return leftHand.toString().equalsIgnoreCase(value);

        return leftHand instanceof String
                && value.equalsIgnoreCase((String) leftHand);
    }

}
