package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.engine.AlicaEngine;

import java.util.HashMap;
import java.util.Vector;

public class ResultEntry {


    private long id;
    AlicaEngine ae;
    HashMap<Long, VarValue> values;
//    mutex valueLock;

    public ResultEntry(long agentID, AlicaEngine ae) {
        this.id = agentID;
        this.ae = ae;
        this.values = new HashMap<>();
    }

    long getId() {
        return id;
    }

    void addValue(long vid, Vector<Integer> result) {
        double now = ae.getIAlicaClock().now().time;
        VarValue vv;
//        lock_guard<std::mutex> lock(valueLock);
        VarValue it = this.values.get(vid);

        if (it != null) {
            vv = it;
            vv.val = result;
            vv.lastUpdate = now;
        }
        else {
            vv = new VarValue(vid, result, now);
            this.values.put(vid, vv);
        }
    }

    void clear() {
        this.values.clear();
    }

    Vector<SolverVar> getCommunicatableResults(long ttl4Communication) {
//        lock_guard<std::mutex> lock(valueLock);
        Vector<SolverVar> lv = new Vector<SolverVar>();
        double now = ae.getIAlicaClock().now().time;

        for(VarValue varValue : values.values()) {

            if(varValue.lastUpdate + ttl4Communication > now) {
                SolverVar sv = new SolverVar();
                sv.id = varValue.id;
                sv.value = varValue.val;
                lv.add(sv);
            }
        }
        return lv;
    }

    Vector<Integer> getValue(long vid, long ttl4Usage) {
        double now = ae.getIAlicaClock().now().time;
//        lock_guard<std::mutex> lock(valueLock);
        VarValue it = this.values.get(vid);

        if(it != null) {
            if(it.lastUpdate + ttl4Usage > now) {
                return it.val;
            }
        }
        return null;
    }

    Vector<Vector<Integer>> getValues(Vector<Variable> query, long ttl4Usage) {
        Vector<Vector<Integer>> ret = new Vector<>(query.size());
//        int i = 0;
        int nans = 0;

//        for(auto it = query.begin(); it != query.end(); it++, i++) {
//
//            ret.get(i) = getValue((it).getID(), ttl4Usage);
//            if(ret.get(i) == null) nans++;
//        }
        for(int i = 0; i < query.size(); i++) {
            ret.set(i,getValue(query.get(i).getID(), ttl4Usage));

            if(ret.get(i) == null)
                nans++;
        }

        if(nans == query.size()-1)
            return null;

        return ret;
    }

}
