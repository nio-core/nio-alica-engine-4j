package de.uniks.vs.jalica.unknown;

import java.util.ArrayList;

/**
 * Created by alex on 18.07.17.
 */
public class SuccessCollection {

    EntryPoint[] entryPoints;
    ArrayList<Long>[] agents;
    int count = 0;

    public SuccessCollection(Plan plan) {
        this.count = plan.getEntryPoints().size();
        this.entryPoints = new EntryPoint[this.count];
        this.agents = new ArrayList[this.count];
        ArrayList<EntryPoint> eps = new ArrayList<>();

//        for (map<long, EntryPoint*>::const_iterator iter = plan.getEntryPoints().begin();
//                iter != plan.getEntryPoints().end(); iter++)

        for (Long key : plan.getEntryPoints().keySet()) {
            eps.add(plan.getEntryPoints().get(key));
        }
        eps.sort(EntryPoint::compareTo);

//        for (EntryPoint ep : eps) {
        for (int i = 0; i < eps.size(); i++) {
            this.entryPoints[i] = eps.get(i);
            this.agents[i] = new ArrayList<>();
        }
    }

    public ArrayList<Long> getAgents(EntryPoint entryPoint) {
        for (int i = 0; i < this.count; i++) {
            if (this.getEntryPoints()[i] == entryPoint) {
                return this.agents[i];
            }
        }
        return null;
    }

    public EntryPoint[] getEntryPoints() {
        return entryPoints;
    }

    public void clear() {
        for (int i = 0; i < this.count; i++)
        {
            this.agents[i].clear();
        }
    }

    public ArrayList<Long>[] getAgents() {
        return agents;
    }

    public int getCount() {
        return count;
    }

    public void setSuccess(long agent, EntryPoint ep) {
        for (int i = 0; i < this.count; i++)
        {
            if (this.entryPoints[i] == ep)
            {
                this.agents[i].add(agent);
                return;
            }
        }
    }

    public ArrayList<Long> getAgentsById(long id) {

        for (int i = 0; i < this.count; i++){

            if (this.getEntryPoints()[i].getID() == id){
                return this.agents[i];
            }
        }
        return null;
    }
}
