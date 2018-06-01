package de.uniks.vs.jalica.unknown;

public class BasicFalseCondition extends BasicCondition {

    @Override
    boolean evaluate(RunningPlan rp) {
        return false;
    }
}
