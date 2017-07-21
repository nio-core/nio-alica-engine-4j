package de.uniks.vs.jalica.common;

import de.uniks.vs.jalica.unknown.EntryPoint;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by alex on 13.07.17.
 */
public class AssignmentCollection {

    public static short maxEpsCount;
    public static Boolean allowIdling;

    private short numEps;
    Vector<Vector<Integer>> robots;
    private ArrayList<EntryPoint> entryPoints;

    public int getSize() {
        return this.numEps;
    }

    public Vector<Integer> getRobots(int index) {
        if (index < this.numEps)
        {
            return this.robots.get(index);
        }
		else
        {
            return null;
        }
    }

    public Vector<Integer> getRobotsByEp(EntryPoint ep) {
        for (int i = 0; i < this.numEps; i++)
        {
            if (this.entryPoints.get(i) == ep)
            {
                return this.robots.get(i);
            }
        }
        return null;
    }

    public void clear() {
        for (int i = 0; i < this.numEps; i++)
        {
            this.robots.get(i).clear();
        }
    }

    public EntryPoint getEp(int index) {
        if (index < this.numEps)
        {
            return this.entryPoints.get(index);
        }
		else
        {
            return null;
        }
    }
}
