package de.uniks.vs.jalica.engine.collections;

import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.model.AbstractPlan;
import de.uniks.vs.jalica.engine.model.EntryPoint;

import java.util.*;

/**
 * Created by alex on 18.07.17.
 */
public class SuccessMarks {

    private LinkedHashMap<AbstractPlan, ArrayList<EntryPoint>> successMarks;

    public SuccessMarks() {
        successMarks = new LinkedHashMap<>();
    }

    public void update( AlicaEngine ae, ArrayList<Long> succeededEps) {
        clear();
        HashMap<Long, EntryPoint> eps = ae.getPlanRepository().getEntryPoints();

        for (long id : succeededEps) {

            EntryPoint ep = eps.get(id);

            if (ep != null) {
                ArrayList<EntryPoint> entryPoints = this.successMarks.get(ep.getPlan());

                if (entryPoints == null) {
                    this.successMarks.put(ep.getPlan(), new ArrayList<EntryPoint>(Arrays.asList(ep)));
                } else {

                    if (entryPoints.contains(ep))
                        entryPoints.add(ep);

                }
            }
//          else {
//                ArrayList<EntryPoint> s = new ArrayList();
//                s.add(ep);
//                this.getSuccessMarks().put(ep.getPlan(), s);
//            }
//        }
        }
    }

//    public update(AlicaEngine ae, ArrayList<Long> epIds) {
//        this.ae = ae;
//        HashMap<Long, EntryPoint> eps = ae.getPlanRepository().getEntryPoints();
//
//        for (long id : epIds) {
//            EntryPoint ep = null;
//            EntryPoint iter = eps.get(id);
//
//            if (iter != null) {
//                ep = eps.get(id);
//
//                ArrayList<EntryPoint> s;
//                ArrayList<EntryPoint> i = this.getSuccessMarks().get(ep.getPlan());
//
//                if (i != null) {
//                    s = this.getSuccessMarks().get(ep.getPlan());
//
////                    if (find(s.begin(), s.end(), ep) == s.end()) {
//                    if (!s.contains(ep))
//                        s.add(ep);
//                }
//            } else {
//                ArrayList<EntryPoint> s = new ArrayList();
//                s.add(ep);
//                this.getSuccessMarks().put(ep.getPlan(), s);
//            }
//        }
//    }

    public void markSuccessfull(AbstractPlan p, EntryPoint e) {
        ArrayList<EntryPoint> l = this.successMarks.get(p);

        if (!l.contains(e)) {
            l.add(e);
        }
//        ArrayList<EntryPoint> iter = this.getSuccessMarks().get(p);//find(p);
//
//        if (iter != this.getSuccessMarks().get(successMarks.size()-1)) {
//            ArrayList<EntryPoint> l = this.getSuccessMarks().get(p);//.at(p);
//            EntryPoint i = find(l, e);
//
//            if (i == l.get(l.size()-1))
//            {
//                l.add(e);
//            }
//        }
//	    else {
//            ArrayList l = new ArrayList<EntryPoint>();
//            l.add(e);
//            this.getSuccessMarks().put(p, l);
//
//        }
    }

//    private EntryPoint find(ArrayList<EntryPoint> l, EntryPoint e) {
//        for (EntryPoint entryPoint: l) {
//            if (entryPoint == e)
//                return entryPoint;
//        }
//
//        return l.get(l.size()-1);
//    }

    public LinkedHashMap<AbstractPlan,ArrayList<EntryPoint>> getSuccessMarks() {
        return successMarks;
    }

    public void removePlan(AbstractPlan plan) {
        this.successMarks.remove(plan);
    }

    boolean succeeded( AbstractPlan p,  EntryPoint e) {
        ArrayList<EntryPoint> entryPoints = this.successMarks.get(p);
        return entryPoints != null ? entryPoints.contains(e) : false;
    }

    public ArrayList<EntryPoint> succeededEntryPoints(AbstractPlan plan) {
        return this.successMarks.get(plan);
    }

    public void clear() {
        successMarks.clear();
    }

    public void limitToPlans(HashSet<AbstractPlan> active) {
        ArrayList<AbstractPlan> plans = new ArrayList<>();

        for (AbstractPlan plan : this.successMarks.keySet()) {

            if (!active.contains(plan)) {
                plans.add(plan);
            }
        }

        for (AbstractPlan plan : plans) {
            this.successMarks.remove(plan);
        }
        
    }

    // <AbstractPlan,ArrayList<EntryPoint>>
    public ArrayList<Long> toIdGrp() {
        ArrayList<Long> ret = new ArrayList<Long>();

        for (ArrayList<EntryPoint> entryPoints : this.getSuccessMarks().values()){

            for (EntryPoint entryPoint : entryPoints) {
                ret.add(entryPoint.getID());
            }
        }
        return ret;
    }
}
