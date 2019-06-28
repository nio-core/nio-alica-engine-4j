package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.common.ConditionVariable;

import java.util.HashMap;
import java.util.Map;

public abstract class ITrigger {

    HashMap<ConditionVariable, Boolean> registeredCVs;

    abstract void run(boolean notifyAll);

    public void registerCV(ConditionVariable condVar) {
        registeredCVs.replace(condVar, false);
    }

    public boolean isNotifyCalled(ConditionVariable cv) {
        return registeredCVs.containsKey(cv) && registeredCVs.get(cv) != null;
    }

    public void setNotifyCalled(boolean called, ConditionVariable cv) {

        if (registeredCVs.containsKey(cv)) {
            registeredCVs.replace(cv, called);
        }
    }

    protected void notifyAll(boolean notifyAll) {
        for ( Map.Entry<ConditionVariable, Boolean> entrySet : registeredCVs.entrySet()) {
            entrySet.setValue(true);

            if (notifyAll) {
                entrySet.getKey().notifyAllThreads();
            } else {
                entrySet.getKey().notifyOneThread();
            }
    }
    }
}
