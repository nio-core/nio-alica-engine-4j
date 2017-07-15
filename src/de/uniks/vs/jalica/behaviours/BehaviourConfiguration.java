package de.uniks.vs.jalica.behaviours;

/**
 * Created by alex on 13.07.17.
 */
public class BehaviourConfiguration {
    private Object parameters;
    private Object variables;
    private Object deferring;
    private int frequency;

    public Object getParameters() {
        return parameters;
    }

    public Object getVariables() {
        return variables;
    }

    public Object getDeferring() {
        return deferring;
    }

    public int getFrequency() {
        return frequency;
    }
}
