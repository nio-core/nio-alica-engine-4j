package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.engine.constrainmodule.ProblemDescriptor;

public abstract class BasicConstraint {

    public abstract void getConstraint(ProblemDescriptor c, RunningPlan rp);
}
