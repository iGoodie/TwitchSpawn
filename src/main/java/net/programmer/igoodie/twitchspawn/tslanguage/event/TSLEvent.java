package net.programmer.igoodie.twitchspawn.tslanguage.event;

import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.TSLFlowNode;

import java.util.LinkedList;
import java.util.List;

public abstract class TSLEvent extends TSLFlowNode {

    protected List<TSLFlowNode> nextNodes;

    public TSLEvent() {
        this.nextNodes = new LinkedList<>();
    }

    public TSLFlowNode append(TSLFlowNode node) {
        nextNodes.add(node);
        return node;
    }

    @Override
    public TSLFlowNode chain(TSLFlowNode next) {
        return append(next);
    }

    public void handleEvent(EventArguments arguments) {
        nextNodes.forEach(node -> System.out.println(node)); // TODO pass args
    }

}
