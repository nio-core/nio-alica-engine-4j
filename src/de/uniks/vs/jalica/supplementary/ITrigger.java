package de.uniks.vs.jalica.supplementary;

import de.uniks.vs.jalica.unknown.condition_variable;

import java.util.HashMap;

/**
 * Created by alex on 17.07.17.
 */
public abstract class ITrigger {

    HashMap<condition_variable, Boolean> registeredCVs = new HashMap<>();

    abstract void run(boolean notifyAll);

    public void registerCV(condition_variable condVar) {
//        lock_guard<mutex> lock(cvVec_mtx);
        registeredCVs.put(condVar,false);
    }

    boolean isNotifyCalled(condition_variable cv) {
        return registeredCVs.containsKey(cv) && registeredCVs.get(cv);
    }

    void setNotifyCalled(boolean called, condition_variable cv) {

        if (registeredCVs.containsKey(cv)) {
            registeredCVs.put(cv, called);
        }
    }

    public abstract boolean isNotifyCalled(Object p0);

    public abstract void setNotifyCalled(boolean called, Object p1);
}
