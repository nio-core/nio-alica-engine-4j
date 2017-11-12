package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.engine.AlicaEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;

/**
 * Created by alex on 18.07.17.
 */
public class SuccessMarks {

    private AlicaEngine ae;
    private LinkedHashMap<AbstractPlan, ArrayList<EntryPoint>> successMarks = new LinkedHashMap<>();

    public SuccessMarks(AlicaEngine ae) {
        this.ae = ae;
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
//                this->getSuccessMarks().begin(); iterator != this->getSuccessMarks().end(); iterator++)

        for (AbstractPlan iterator : this.getSuccessMarks().keySet()) {

            if (iterator == plan)
            {
                return this.getSuccessMarks().get(iterator);
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
                ret.add(entryPoint.getId());
            }
        }
        return ret;
    }
}
