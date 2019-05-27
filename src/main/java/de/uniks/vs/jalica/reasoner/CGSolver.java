package de.uniks.vs.jalica.reasoner;

import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.common.CommonUtils;
import de.uniks.vs.jalica.engine.common.SolverVariable;
import de.uniks.vs.jalica.engine.model.Variable;
import de.uniks.vs.jalica.engine.constrainmodule.ProblemDescriptor;
import de.uniks.vs.jalica.engine.constrainmodule.Solver;

import java.util.Vector;

/**
 * Created by alex on 13.07.17.
 */
public class CGSolver extends Solver {

    GSolver gs;
    GSolver sgs;

    double lastUtil;
    double lastRuns;
    double lastFEvals;

    public CGSolver(AlicaEngine ae) {
        super(ae);
    }

    boolean existsSolution(Vector<Variable> vars, Vector<ProblemDescriptor> calls) {
        CommonUtils.aboutNoImpl();
        return false;
    }

    boolean getSolution(Vector<Variable> vars, Vector<ProblemDescriptor> calls, Vector results) {
        CommonUtils.aboutNoImpl();
        return false;
    }

    SolverVariable createVariable(long id) {
        CommonUtils.aboutNoImpl();
        return null;
    }
}
