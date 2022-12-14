package de.uniks.vs.jalica.engine;

/**
 * Created by alex on 20.07.17.
 */
public abstract class BasicCondition {


    public abstract boolean evaluate(RunningPlan rp);

    boolean isStateTimedOut( long timeOut, RunningPlan rp) {

        if (rp.getStateStartTime().time == 0)
            return false;
        double time = (long) (rp.getAlicaEngine().getAlicaClock().now()).time;
        double timeDiff = time - (rp.getStateStartTime()).time;

        if (timeDiff > timeOut) {
            return true;
        }
        return false;
    }

    boolean isTimeOut( long timeOut,  long startTime, RunningPlan rp) {

        if (startTime == 0)
            return false;
        double time = (rp.getAlicaEngine().getAlicaClock().now()).time;
        double timeDiff = time - (startTime);

        if (timeDiff > timeOut) {
            return true;
        }
        return false;
    }
}
