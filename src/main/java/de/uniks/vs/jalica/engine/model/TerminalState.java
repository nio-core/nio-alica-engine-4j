package de.uniks.vs.jalica.engine.model;

import de.uniks.vs.jalica.engine.model.PostCondition;
import de.uniks.vs.jalica.engine.model.State;

/**
 * Created by alex on 03.08.17.
 */
public class TerminalState extends State {

    private PostCondition postCondition;

    public TerminalState() {
        super();
        this.terminal = true;
        this.postCondition = null;
    }

    public void setPostCondition(PostCondition postCondition) {
        this.postCondition = postCondition;
    }

    public PostCondition getPostCondition() {
        return postCondition;
    }
}
