package de.uniks.vs.jalica.supplementary;

import de.uniks.vs.jalica.unknown.ConditionVariable;

import java.util.HashMap;

/**
 * Created by alex on 17.07.17.
 */
public abstract class Trigger {

    HashMap<ConditionVariable, Boolean> registeredCVs = new HashMap<>();

    public void addConditionVariable(ConditionVariable cv) {
        registeredCVs.put(cv, false);
    }

    public boolean isNotifyCalled(ConditionVariable cv) {
        return registeredCVs.containsKey(cv) && registeredCVs.get(cv);
    }

    public void setNotifyCalled(boolean called, ConditionVariable cv) {

        if (registeredCVs.containsKey(cv)) {
            registeredCVs.put(cv, called);
        }
    }

    protected void notifyAll(boolean notifyAll) {

        for (ConditionVariable cv : registeredCVs.keySet()) {
            registeredCVs.put(cv, true);

            if (notifyAll) {
                cv.notifyAllThreads();
            }
            else {
                cv.notifyOneThread();
            }
        }
    }
}
