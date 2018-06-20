package de.uniks.vs.jalica.unknown;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by alex on 18.07.17.
 */
public class SuccessCollection {

    ArrayList<EntryPoint> entryPoints;
    Vector<ArrayList<Integer>> agents;
    int count = 0;

    public SuccessCollection(Plan plan) {
        this.count = plan.getEntryPoints().size();
        this.entryPoints = new ArrayList<EntryPoint>(this.count);
        this.agents = new Vector<ArrayList<Integer>>(this.count);
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
            this.entryPoints.add(i, ep);
            this.agents.add(i, new ArrayList<Integer>());
            i++;
        }
    }

    public ArrayList<Integer> getAgents(EntryPoint entryPoint) {
        for (int i = 0; i < this.count; i++) {
            if (this.getEntryPoints().get(i) == entryPoint) {
                return this.agents.get(i);
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
            this.agents.get(i).clear();
        }
    }

    public Vector<ArrayList<Integer>> getAgents() {
        return agents;
    }

    public int getCount() {
        return count;
    }

    public void setSuccess(int agent, EntryPoint ep) {
        for (int i = 0; i < this.count; i++)
        {
            if (this.entryPoints.get(i) == ep)
            {
                this.agents.get(i).add(agent);
                return;
            }
        }
    }
}
