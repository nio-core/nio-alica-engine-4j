package de.uniks.vs.jalica.unknown;

/**
 * Created by alex on 03.08.17.
 */
public class TerminalState extends State{
    private PostCondition postCondition;

    public void setPostCondition(PostCondition postCondition) {
        this.postCondition = postCondition;
    }
}
