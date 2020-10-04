package net.programmer.igoodie.twitchspawn.tslanguage.predicate;

import net.programmer.igoodie.twitchspawn.TwitchSpawn;
import net.programmer.igoodie.twitchspawn.tslanguage.event.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.TSLFlowNode;
import net.programmer.igoodie.twitchspawn.tslanguage.keyword.TSLPredicateProperty;
import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxError;

public class TSLPredicate implements TSLFlowNode {

    public TSLComparator comparator;
    public String fieldAlias;
    private TSLFlowNode next;

    public TSLPredicate(String fieldAlias, TSLComparator comparator) throws TSLSyntaxError {
        if (!TSLPredicateProperty.exists(fieldAlias))
            throw new TSLSyntaxError("Unexpected predicate field alias -> " + fieldAlias);

        this.fieldAlias = fieldAlias;
        this.comparator = comparator;
    }

    public TSLFlowNode getNext() {
        return next;
    }

    @Override
    public TSLFlowNode chain(TSLFlowNode next) {
        this.next = next;
        return next;
    }

    @Override
    public boolean process(EventArguments args) {
        TwitchSpawn.LOGGER.debug("Reached TSLPredicate node -> {} with {}",
                comparator.getClass().getSimpleName(), args);

        Object value = TSLPredicateProperty.extractFrom(args, fieldAlias);

        if (comparator.compare(value))
            return next.process(args);

        return false;
    }

    @Override
    public boolean willPerform(EventArguments args) {
        Object value = TSLPredicateProperty.extractFrom(args, fieldAlias);
        return comparator.compare(value) && next.willPerform(args);
    }

}
