package de.uniks.vs.jalica.unknown;

/**
 * Created by alex on 20.07.17.
 */
public abstract class BasicCondition {

    abstract boolean evaluate(RunningPlan rp);

    boolean isStateTimedOut( long timeOut, RunningPlan rp) {

        if (rp.getStateStartTime().time == 0)
            return false;
        double time = (long) (rp.getAlicaEngine().getIAlicaClock().now()).time;
        double timeDiff = time - (rp.getStateStartTime()).time;

        if (timeDiff > timeOut) {
            return true;
        }
        return false;
    }

    boolean isTimeOut( long timeOut,  long startTime, RunningPlan rp) {

        if (startTime == 0)
            return false;
        double time = (rp.getAlicaEngine().getIAlicaClock().now()).time;
        double timeDiff = time - (startTime);

        if (timeDiff > timeOut) {
            return true;
        }
        return false;
    }
}
