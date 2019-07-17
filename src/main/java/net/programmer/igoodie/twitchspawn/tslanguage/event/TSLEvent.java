package net.programmer.igoodie.twitchspawn.tslanguage.event;

import net.programmer.igoodie.twitchspawn.tslanguage.EventArguments;
import net.programmer.igoodie.twitchspawn.tslanguage.TSLFlowNode;

import java.util.LinkedList;
import java.util.List;

public abstract class TSLEvent {

    protected String eventType;
    protected String eventFor;

    protected List<TSLFlowNode> nextNodes;

    public TSLEvent(String eventType, String eventFor) {
        this.nextNodes = new LinkedList<>();
        this.eventType = eventType;
        this.eventFor = eventFor;
    }

    public void append(TSLFlowNode node) {
        nextNodes.add(node);
    }

    public void handleEvent(EventArguments arguments) {
        nextNodes.forEach(node -> System.out.println(node)); // TODO pass args
    }

}
