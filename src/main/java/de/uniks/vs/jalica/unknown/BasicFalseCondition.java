package de.uniks.vs.jalica.unknown;

public class BasicFalseCondition extends BasicCondition {

    @Override
    protected boolean evaluate(RunningPlan rp) {
        return false;
    }

}
