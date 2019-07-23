package net.programmer.igoodie.twitchspawn.tslanguage.predicate;

import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

public abstract class BasicComparator extends TSLComparator {

    public double value;

    public BasicComparator(String rightHandRaw) throws TSLSyntaxError {
        try {
            this.value = Double.parseDouble(rightHandRaw);

        } catch (NumberFormatException e) {
            throw new TSLSyntaxError("Expected a valid fractional number, found " + rightHandRaw + " instead.");
        }
    }

}
