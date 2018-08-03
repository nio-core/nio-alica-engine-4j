package de.uniks.vs.jalica.unknown;

/**
 * Created by alex on 03.08.17.
 */
public class FailureState extends TerminalState {

    public FailureState() {
        this.terminal = true;
        this.successState = false;
        this.failureState = true;
    }

    @Override
    public String toString() {
        String  ss = "#FailureState: " + this.name + " " + this.id + "\n";
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
