package de.uniks.vs.jalica.engine.model;

/**
 * Created by alex on 03.08.17.
 * Updated 23.6.19
 */
public class FailureState extends TerminalState {

    public FailureState() {
        super(StateType.FAILURE);
    }

    @Override
    public String toString() {
        String  ss = "#FailureState: " + this.getName() + " " + this.id + "\n";
        ss += "\t Result:" + "\n";
        ss += "\t InTransitions: " + this.inTransitions.size() + "\n";
        if(this.inTransitions.size() != 0)
        {
            for(Transition t : this.getInTransitions())
            {
                ss += "\t" + t.getID() + " " + t.getName() + "\n";
            }
        }
        ss += "#EndFailureState" + "\n";
        return ss;
    }
}
