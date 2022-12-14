package de.uniks.vs.jalica.engine.constrainmodule;

import de.uniks.vs.jalica.engine.RunningPlan;
import de.uniks.vs.jalica.engine.model.Condition;
import de.uniks.vs.jalica.engine.model.Variable;
import de.uniks.vs.jalica.common.utils.CommonUtils;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by alex on 10.11.17.
 */
public class ProblemPart {
    Condition condition;
    /**
     *  Hierarchie: 1.vector< 2.list< 3.vector< 4.Variable* > > >
     * 1. Vector of Quantors, e.g., For all agents in state S variables X,Y exist.
     * 2. List of Agents, e.g., An agent has variables X,Y.
     * 3. Vector of Variables, e.g., variables X,Y.
     * 4. Variable, e.g., variable X.
     */
    Vector<ArrayList<Vector<Variable>>> domainVariables;
    RunningPlan runningplan;
    Vector<Vector<Integer>> agentsInScope;

    ProblemPart(Condition con, RunningPlan rp){
        CommonUtils.aboutNoImpl();
    }
}
