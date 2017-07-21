package de.uniks.vs.jalica.unknown;

/**
 * Created by alex on 17.07.17.
 */
public class AbstractPlan extends AlicaElement {
    private PreCondition preCondition;
    private RuntimeCondition runtimeCondition;

    public PreCondition getPreCondition() {
        return preCondition;
    }

    public RuntimeCondition getRuntimeCondition() {
        return runtimeCondition;
    }
}
