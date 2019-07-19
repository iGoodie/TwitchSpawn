package net.programmer.igoodie.twitchspawn.tslanguage.event;

import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.TSLFlowNode;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class TSLEvent implements TSLFlowNode {

    protected List<TSLFlowNode> nextNodes;

    public TSLEvent() {
        this.nextNodes = new LinkedList<>();
    }

    @Override
    public TSLFlowNode chain(TSLFlowNode next) {
        nextNodes.add(next);
        return next;
    }

    @Override
    public boolean process(EventArguments args) {
        Iterator<TSLFlowNode> iterator = nextNodes.iterator();
        boolean success = false;

        while(iterator.hasNext()) {
            success |= iterator.next().process(args);
        }

        return success;
    }

}
