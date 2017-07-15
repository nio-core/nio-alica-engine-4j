package de.uniks.vs.jalica.behaviours;

/**
 * Created by alex on 14.07.17.
 */
public class BasicBehaviour extends IBehaviourCreator{
    private Object parameters;
    private Object variables;
    private Object delayedStart;
    private int interval;

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
}
