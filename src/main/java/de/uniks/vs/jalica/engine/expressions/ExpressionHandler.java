package de.uniks.vs.jalica.engine.expressions;

import de.uniks.vs.jalica.engine.*;
import de.uniks.vs.jalica.engine.model.Condition;
import de.uniks.vs.jalica.engine.model.Plan;
import de.uniks.vs.jalica.engine.model.Transition;

/**
 * Created by alex on 13.07.17.
 * Updated 23.6.19
 */
public class ExpressionHandler {

    private final IConditionCreator conditionCreator;
    private final IUtilityCreator utilityCreator;
    private final IConstraintCreator constraintCreator;
    private final AlicaEngine ae;

    public ExpressionHandler(AlicaEngine ae, IConditionCreator cc, IUtilityCreator uc, IConstraintCreator crc) {
        this.ae = ae;
        this.conditionCreator = cc;
        this.utilityCreator = uc;
        this.constraintCreator = crc;
    }

    public void attachAll() {
        PlanRepository pr = ae.getPlanRepository();

        for (Plan p : pr.getPlans().values()) {
            BasicUtilityFunction ufGen = utilityCreator.createUtility(p.getID());
            p.setUtilityFunction(ufGen.getUtilityFunction(p));

            if (p.getPreCondition() != null) {

                if (p.getPreCondition().isEnabled()) {
                    p.getPreCondition().setBasicCondition(
                        this.conditionCreator.createConditions(p.getPreCondition().getID()));
                    attachConstraint(p.getPreCondition());
                }
				else {
                    p.getPreCondition().setBasicCondition(new BasicFalseCondition());
                }
            }

            if (p.getRuntimeCondition() != null) {
                p.getRuntimeCondition().setBasicCondition(
                    this.conditionCreator.createConditions(p.getRuntimeCondition().getID()));
                attachConstraint(p.getRuntimeCondition());
            }

            for (Transition t : p.getTransitions()) {

                if (t.getPreCondition() != null) {

                    if (t.getPreCondition().isEnabled()) {
                        t.getPreCondition().setBasicCondition(this.conditionCreator.createConditions(t.getPreCondition().getID()));
                        attachConstraint(t.getPreCondition());
                    }
					else {
                        t.getPreCondition().setBasicCondition(new BasicFalseCondition());
                    }
                }
            }
        }

    }

    boolean dummyTrue(RunningPlan rp) {
        return true;
    }

    boolean dummyFalse(RunningPlan rp) {
        return false;
    }

    private void attachConstraint(Condition c) {

        if (c.getVariables().size() == 0 && c.getQuantifiers().size() == 0) {
            c.setBasicConstraint(new DummyConstraint());
        }
        else {
            c.setBasicConstraint(this.constraintCreator.createConstraint(c.getID()));
        }
    }
}
