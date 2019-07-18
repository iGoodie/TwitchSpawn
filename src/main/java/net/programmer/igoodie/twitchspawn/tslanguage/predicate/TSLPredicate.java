package net.programmer.igoodie.twitchspawn.tslanguage.predicate;

import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.TSLFlowNode;

public class TSLPredicate extends TSLFlowNode {

    public TSLComparator comparator;
    public String parameterName;
    private TSLFlowNode next;

    public TSLPredicate(String parameterName, TSLComparator comparator) {
        this.parameterName = parameterName;
        this.comparator = comparator;
    }

    public TSLFlowNode getNext() {
        return next;
    }

    public TSLFlowNode setNext(TSLFlowNode next) {
        this.next = next;
        return next;
    }

    @Override
    public TSLFlowNode chain(TSLFlowNode next) {
        return setNext(next);
    }

    public boolean test(EventArguments eventArguments) {
        return true;
    }

}
