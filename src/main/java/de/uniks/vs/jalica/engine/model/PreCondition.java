package de.uniks.vs.jalica.engine.model;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by alex on 20.07.17.
 * Updated 23.6.19
 */
public class PreCondition extends Condition {

    private boolean enabled;

    public PreCondition() {
        enabled = true;
    }

    public boolean isEnabled() {
        return  enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return  "#PreCondition: " + this.getName() + " " + this.id + (this.enabled ? "enabled" : "disabled")
                + "\n\t ConditionString: " + this.conditionString + "\n#EndPreCondition\n";
    }
}
