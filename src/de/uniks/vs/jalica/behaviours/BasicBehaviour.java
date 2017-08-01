package de.uniks.vs.jalica.behaviours;

import de.uniks.vs.jalica.supplementary.ITrigger;
import de.uniks.vs.jalica.supplementary.Timer;
import de.uniks.vs.jalica.unknown.RunningPlan;

/**
 * Created by alex on 14.07.17.
 */
public class BasicBehaviour implements IBehaviourCreator {
    private Object parameters;
    private Object variables;
    private Object delayedStart;
    private int interval;
    private RunningPlan runningPlan;
    private boolean success;
    private boolean failure;
    private boolean callInit;
    private ITrigger behaviourTrigger;
    private Timer timer;
    private boolean running;


    public void setParameters(Object parameters) {
        this.parameters = parameters;
    }

    public void setVariables(Object variables) {
        this.variables = variables;
    }

    public void setDelayedStart(Object delayedStart) {
        this.delayedStart = delayedStart;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public void setRunningPlan(RunningPlan runningPlan) {
        this.runningPlan = runningPlan;
    }

    public boolean start() {
        this.callInit = true;
        if (behaviourTrigger == null)
        {
            return this.timer.start();
        }
        else
        {
            this.running = true;
        }
        return true;
    }

    public boolean stop() {
        this.success = false;
        this.failure = false;
        if (behaviourTrigger == null)
        {
            return this.timer.stop();
        }
        else
        {
            this.running = false;
        }
        return true;
    }

    public boolean isSuccess() {
        return success && !this.callInit;
    }

    @Override
    public IBehaviourCreator createBehaviour(Long key) {
        return null;
    }

    public boolean isFailure() {
        return failure && !this.callInit;
    }
}
