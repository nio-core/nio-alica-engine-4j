package de.uniks.vs.jalica.engine.model;

import de.uniks.vs.jalica.engine.model.PostCondition;
import de.uniks.vs.jalica.engine.model.State;

/**
 * Created by alex on 03.08.17.
 * Updated 23.6.19
 */
public class TerminalState extends State {

    private PostCondition postCondition;

    public TerminalState(StateType t) {
        super(t);
        this.postCondition = null;
    }

    public void setPostCondition(PostCondition postCondition) {
        this.postCondition = postCondition;
    }

    public PostCondition getPostCondition() {
        return postCondition;
    }
}
