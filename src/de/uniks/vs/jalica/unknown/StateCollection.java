package de.uniks.vs.jalica.unknown;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Created by alex on 17.07.17.
 */
public class StateCollection {

    private Vector<Integer> robots;
    private Vector<State> states;

    public Set<Integer> getRobotsInState(State s) {

        Set<Integer> ret = new HashSet<>();
        for (int i = 0; i < this.robots.size(); i++)
        {
            if (this.states.get(i) == s)
            {
                ret.add(this.robots.get(i));
            }
        }
        return ret;
    }

    public void setStates(Vector<State> states) {
        this.states = states;
    }


    public void setState(int robot, State state) {
        for (int i = 0; i < this.robots.size(); i++)
        {
            if (this.robots.get(i) == robot)
            {
                this.states.set(i, state);
                return;
            }
        }
        this.robots.add(robot);
        this.states.add(state);
    }

    public void removeRobot(int r) {
        for(int i = 0; i < this.states.size();i++)
        {
            if(this.robots.get(i) == r)
            {
                this.robots.remove( i);
                this.states.remove( i);
                return;
            }
        }
    }

    public void clear() {
        this.robots.clear();
        this.states.clear();
    }
}
