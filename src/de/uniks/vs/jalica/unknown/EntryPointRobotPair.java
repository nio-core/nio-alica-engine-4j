package de.uniks.vs.jalica.unknown;

import java.util.Vector;

/**
 * Created by alex on 31.07.17.
 */
public class EntryPointRobotPair {
    private final EntryPoint entryPoint;
    private final int robot;

    public EntryPointRobotPair(EntryPoint ep, int r) {
        this.entryPoint = ep;
        this.robot = r;
    }

    public  boolean equals( EntryPointRobotPair other) {
        if (other == null)
        {
            return false;
        }
        if (other.entryPoint.getId() != this.entryPoint.getId())
            return false;
        return (other.getRobot() == this.robot);
    }

    public int getRobot() {return robot;}

    public boolean containedIn(Vector<EntryPointRobotPair> entryPointRobots) {

        for (EntryPointRobotPair entryPointRobotPair: entryPointRobots) {

            if (this.equals(entryPointRobotPair))
                return true;
        }
        return false;
    }

    public EntryPoint getEntryPoint() {return entryPoint;}
}
