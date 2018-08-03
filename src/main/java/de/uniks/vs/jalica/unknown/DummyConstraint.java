package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.reasoner.ProblemDescriptor;

public class DummyConstraint extends BasicConstraint {

    public void getConstraint(ProblemDescriptor c, RunningPlan rp) {
        CommonUtils.aboutNoImpl();
    }
}
