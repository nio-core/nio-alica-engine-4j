package de.uniks.vs.jalica.engine.expressions;

import de.uniks.vs.jalica.engine.BasicConstraint;
import de.uniks.vs.jalica.engine.RunningPlan;
import de.uniks.vs.jalica.engine.constrainmodule.ProblemDescriptor;
import de.uniks.vs.jalica.common.utils.CommonUtils;

public class DummyConstraint extends BasicConstraint {

    public void getConstraint(ProblemDescriptor c, RunningPlan rp) {
        CommonUtils.aboutNoImpl();
    }
}
