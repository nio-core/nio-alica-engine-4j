package de.uniks.vs.jalica.unknown;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by alex on 20.07.17.
 */
public class PreCondition extends Condition {

    boolean enabled;

    public PreCondition() {
        variables = new Vector<>();
        quantifiers = new ArrayList<>();
    }

    PreCondition(long id) {
        super();
        this.id = id;
        this.enabled = true;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return  enabled;
    }

    @Override
    public String toString() {
        return  "#PreCondition: " + this.name + " " + this.id + (this.enabled ? "enabled" : "disabled")+ "\n\t ConditionString: " + this.conditionString + "\n#EndPreCondition\n";
    }
}
