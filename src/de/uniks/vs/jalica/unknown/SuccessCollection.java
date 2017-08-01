package de.uniks.vs.jalica.unknown;

import com.sun.org.apache.bcel.internal.generic.ARRAYLENGTH;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by alex on 18.07.17.
 */
public class SuccessCollection {

    ArrayList<EntryPoint> entryPoints;
    Vector<ArrayList<Integer>> robots;
    int count = 0;

    public SuccessCollection(Plan plan) {
        this.count = plan.getEntryPoints().size();
        this.entryPoints = new ArrayList<EntryPoint>(this.count);
        this.robots = new Vector<ArrayList<Integer>>(this.count);
        int i = 0;
        ArrayList<EntryPoint> eps = new ArrayList<>();

//        for (map<long, EntryPoint*>::const_iterator iter = plan.getEntryPoints().begin();
//                iter != plan.getEntryPoints().end(); iter++)

        for (Long key : plan.getEntryPoints().keySet()) {
            eps.add(plan.getEntryPoints().get(key));
        }
        eps.sort(EntryPoint::compareTo);
        for (EntryPoint ep : eps)
        {
            this.entryPoints.set(i, ep);
            this.robots.set(i, new ArrayList<Integer>());
            i++;
        }
    }

    public ArrayList<Integer> getRobots(EntryPoint entryPoint) {
        for (int i = 0; i < this.count; i++) {
            if (this.getEntryPoints().get(i) == entryPoint) {
                return this.robots.get(i);
            }
        }
        return null;
    }

    public ArrayList<EntryPoint> getEntryPoints() {
        return entryPoints;
    }

    public void clear() {
        for (int i = 0; i < this.count; i++)
        {
            this.robots.get(i).clear();
        }
    }

    public Vector<ArrayList<Integer>> getRobots() {
        return robots;
    }

    public int getCount() {
        return count;
    }

    public void setSuccess(int robot, EntryPoint ep) {
        for (int i = 0; i < this.count; i++)
        {
            if (this.entryPoints.get(i) == ep)
            {
                this.robots.get(i).add(robot);
                return;
            }
        }
    }
}
