package de.uniks.vs.jalica.engine.expressions;

import de.uniks.vs.jalica.engine.BasicCondition;
import de.uniks.vs.jalica.engine.RunningPlan;

public class BasicFalseCondition extends BasicCondition {

    @Override
    public boolean evaluate(RunningPlan rp) {
        return false;
    }

}
