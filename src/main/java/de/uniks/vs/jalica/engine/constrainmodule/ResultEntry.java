package de.uniks.vs.jalica.engine.constrainmodule;

import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.AlicaTime;
import de.uniks.vs.jalica.engine.model.Variable;
import de.uniks.vs.jalica.engine.containers.SolverVar;
import de.uniks.vs.jalica.engine.common.VarValue;

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

    void addValue(long vid, Vector<Integer> result, AlicaTime time) {
//        double now = ae.getAlicaClock().now().time;
//        lock_guard<std::mutex> lock(valueLock);
        VarValue existingVarValue = this.values.get(vid);

        if (existingVarValue != null) {
            VarValue varValue = existingVarValue;
            varValue.val = result;
            varValue.lastUpdate = time;
        }
        else {
            VarValue varValue = new VarValue(vid, result, time);
            this.values.put(vid, varValue);
        }
    }

    void clear() {
        this.values.clear();
    }

    Vector<SolverVar> getCommunicatableResults(long earliest) {
        Vector<SolverVar> lv = new Vector<SolverVar>();
//        double now = ae.getAlicaClock().now().time;

        for(VarValue varValue : values.values()) {

            if (varValue.lastUpdate.time > earliest) {
//            if (varValue.lastUpdate.time + ttl4Communication > now) {
                SolverVar sv = new SolverVar();
                sv.id = varValue.id;
                sv.value = varValue.val;
                lv.add(sv);
            }
        }
        return lv;
    }

    Vector<Integer> getValue(long vid, long ttl4Usage) {
        double now = ae.getAlicaClock().now().time;
//        lock_guard<std::mutex> lock(valueLock);
        VarValue it = this.values.get(vid);

        if(it != null) {
            if(it.lastUpdate.time + ttl4Usage > now) {
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
//            ret.get(i) = getValue((it).extractID(), ttl4Usage);
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
