package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.engine.AlicaEngine;

import java.util.*;

/**
 * Created by alex on 18.07.17.
 */
public class SuccessMarks {

    private AlicaEngine ae;
    private LinkedHashMap<AbstractPlan, ArrayList<EntryPoint>> successMarks = new LinkedHashMap<>();

    public SuccessMarks(AlicaEngine ae) {
        this.ae = ae;
    }

    public SuccessMarks(AlicaEngine ae, ArrayList<Long> epIds) {
        this.ae = ae;
        HashMap<Long, EntryPoint> eps = ae.getPlanRepository().getEntryPoints();

        for (long id : epIds) {
            EntryPoint ep = null;
            EntryPoint iter = eps.get(id);

            if (iter != null) {
                ep = eps.get(id);

                ArrayList<EntryPoint> s;
                ArrayList<EntryPoint> i = this.getSuccessMarks().get(ep.getPlan());

                if (i != null) {
                    s = this.getSuccessMarks().get(ep.getPlan());

//                    if (find(s.begin(), s.end(), ep) == s.end()) {
                    if (!s.contains(ep))
                        s.add(ep);
                }
            } else {
                ArrayList<EntryPoint> s = new ArrayList();
                s.add(ep);
                this.getSuccessMarks().put(ep.getPlan(), s);
            }
        }
    }

    public void markSuccessfull(AbstractPlan p, EntryPoint e) {
        ArrayList<EntryPoint> iter = this.getSuccessMarks().get(p);//find(p);

        if (iter != this.getSuccessMarks().get(successMarks.size()-1)) {
            ArrayList<EntryPoint> l = this.getSuccessMarks().get(p);//.at(p);
            EntryPoint i = find(l, e);
//            auto i = find(l.begin(), l.end(), e);

            if (i == l.get(l.size()-1))
            {
                l.add(e);
            }
        }
	    else {
            ArrayList l = new ArrayList<EntryPoint>();
            l.add(e);
            this.getSuccessMarks().put(p, l);

        }
    }

    private EntryPoint find(ArrayList<EntryPoint> l, EntryPoint e) {
        for (EntryPoint entryPoint: l) {
            if (entryPoint == e)
                return entryPoint;
        }

        return l.get(l.size()-1);
    }

    public LinkedHashMap<AbstractPlan,ArrayList<EntryPoint>> getSuccessMarks() {
        return successMarks;
    }

    public void removePlan(AbstractPlan plan) {
        this.getSuccessMarks().remove(plan);
    }

    public ArrayList<EntryPoint> succeededEntryPoints(AbstractPlan plan) {

//        for (map<AbstractPlan*, shared_ptr<list<EntryPoint*> > >::const_iterator iterator =
//                this->getSuccessMarks().begin(); iterator != this.getSuccessMarks().end(); iterator++)

        for (AbstractPlan abstractPlan : this.getSuccessMarks().keySet()) {

            if (abstractPlan == plan) {
                return this.getSuccessMarks().get(abstractPlan);
            }
        }
        return null;
    }

    public void clear() {
        successMarks.clear();
    }

    public void limitToPlans(HashSet<AbstractPlan> active) {
        ArrayList<AbstractPlan> tr = new ArrayList<>();

        for (AbstractPlan itePlan : this.getSuccessMarks().keySet()) {

            if (!active.contains(itePlan)) {
                tr.add(itePlan);
            }
        }

        for (AbstractPlan p : tr) {
            this.getSuccessMarks().remove(p);
        }
        
    }

    // <AbstractPlan,ArrayList<EntryPoint>>
    public ArrayList<Long> toList() {
        ArrayList<Long> ret = new ArrayList<Long>();

        for (ArrayList<EntryPoint> entryPoints : this.getSuccessMarks().values()){

            for (EntryPoint entryPoint : entryPoints) {
                ret.add(entryPoint.getID());
            }
        }
        return ret;
    }
}
