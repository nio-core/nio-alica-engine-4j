package de.uniks.vs.jalica.common;

import de.uniks.vs.jalica.common.utils.CommonUtils;

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
//        if (CommonUtils.TE_DEBUG_debug)CommonUtils.aboutCallNotification("isNotfiyCalled " + (registeredCVs.containsKey(cv) && registeredCVs.get(cv)) );
        return registeredCVs.containsKey(cv) && registeredCVs.get(cv);
    }

    public void setNotifyCalled(boolean called, ConditionVariable cv) {

        if (registeredCVs.containsKey(cv)) {
//            if (CommonUtils.TE_DEBUG_debug) CommonUtils.aboutCallNotification("setNotifyCalled " + called);
            registeredCVs.put(cv, called);
        }
    }

    protected void notifyAll(boolean notifyAll) {

        for (ConditionVariable cv : registeredCVs.keySet()) {
            if (CommonUtils.TE_DEBUG_debug && false)  CommonUtils.aboutCallNotification("notifyAll " + notifyAll);

            registeredCVs.replace(cv, true);

            if (notifyAll) {
                cv.notifyAllThreads();
            }
            else {
                cv.notifyOneThread();
            }
        }
    }
}
