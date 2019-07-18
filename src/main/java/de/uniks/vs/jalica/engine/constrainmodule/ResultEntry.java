package de.uniks.vs.jalica.engine.constrainmodule;

import de.uniks.vs.jalica.engine.AlicaTime;
import de.uniks.vs.jalica.engine.common.VarValue;
import de.uniks.vs.jalica.engine.containers.SolverVar;
import de.uniks.vs.jalica.engine.idmanagement.ID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 24.6.19
 */

public class ResultEntry {

    private ID id;
    HashMap<Long, VarValue> values;
    Lock valueLock;

    public ResultEntry(ID robotId) {
        this.id = robotId;
        this.values = new HashMap<>();
    }

    public ResultEntry(ResultEntry o) {
        this.id = o.id;
        this.values = new HashMap<>(o.values);
        o.values.clear();
        this.valueLock = new ReentrantLock();
    }

    public ID getId() {
        return this.id;
    }

    ResultEntry tranfer(ResultEntry o) {
        this.id = o.id;
        this.values = new HashMap<>(o.values);
        o.values.clear();
        return this;
    }

    void addValue(long vid, Variant val, AlicaTime time) {
//        std::lock_guard<std::mutex> lock(_valueLock);
        synchronized (this.values) {
            VarValue it = this.values.get(vid);
            if (it != null) {
                VarValue vv = it;
                vv.val = val;
                vv.lastUpdate = time;
            } else {
                this.values.put(vid, new VarValue(val, time));
            }
        }
    }

    void clear() {
//        std::lock_guard<std::mutex> lock(_valueLock);
        synchronized (this.values) {
            this.values.clear();
        }
    }

    void getCommunicatableResults(AlicaTime earliest, ArrayList<SolverVar> oResult) {
//        std::lock_guard<std::mutex> lock(_valueLock);
        synchronized (this.values) {
            for (Map.Entry<Long, VarValue> p : this.values.entrySet()) {
                if (p.getValue().lastUpdate.time > earliest.time) {
                    SolverVar sv = new SolverVar();
                    sv.id = p.getKey();
                    p.getValue().val.serializeTo(sv.value);
                    oResult.add(sv);
                }

            }
        }
    }

    Variant getValue(long vid, AlicaTime earliest) {
//        std::lock_guard < std::mutex > lock(_valueLock);
        synchronized (this.values) {
            VarValue it = this.values.get(vid);
            if (it != null) {
                if (it.lastUpdate.time > earliest.time) {
                    return it.val;
                }
            }
        }
        return new Variant();
    }


}
