package de.uniks.vs.jalica.engine.model;

/**
 * Created by alex on 03.08.17.
 */
public class SuccessState extends TerminalState {

    public SuccessState() {
        this.terminal = true;
        this.successState = true;
        this.failureState = false;
    }

    public String toString()
    {
        String ss = "#SuccessState: " + this.name + " " + this.id + "\n";
        ss += "\t Result:" + "\n";
        ss += "\t InTransitions: " + this.inTransitions.size() + "\n";
        if(this.inTransitions.size() != 0)
        {
            for(Transition t : this.getInTransitions())
            {
                ss += "\t" + t.getID() + " " + t.getName() + "\n";
            }
        }
        ss += "#SuccessState" + "\n";
        return ss;
    }
}
