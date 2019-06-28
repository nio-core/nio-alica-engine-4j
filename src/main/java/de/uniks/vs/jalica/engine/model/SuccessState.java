package de.uniks.vs.jalica.engine.model;

/**
 * Created by alex on 03.08.17.
 */
public class SuccessState extends TerminalState {

    public SuccessState() {
        super(StateType.SUCCESS);
    }

    public String toString() {
        String indent = "";
        String ss = "";
        ss += indent +  "#SuccessState: " + getName() + " " + getID() + "\n";
        ss += indent + "\t Result:" + "\n";
        ss += indent + "\t InTransitions: " + getInTransitions().size() + "\n";

        if (getInTransitions().size() != 0) {

            for (Transition t : getInTransitions()) {
                ss += indent + "\t" + t.getID() + " " + t.getName() + "\n";
            }
        }
        ss += "#SuccessState" + "\n";
        return ss;
    }
}
