package net.programmer.igoodie.twitchspawn.tslanguage;

public interface TSLFlowNode {

    /**
     * Chains given node to this one
     *
     * @param next Next node
     * @return The next node if chained successfully.
     * Returns null otherwise
     */
    default TSLFlowNode chain(TSLFlowNode next) {
        throw new UnsupportedOperationException("TSLFlowNode::chain is not meant to be used on "
                + getClass().getSimpleName());
    }

    /**
     * Processes given event arguments.
     * Passes them to next flow node(s) if necessary
     *
     * @return True if successfully processed given args
     */
    boolean process(EventArguments args);

}
