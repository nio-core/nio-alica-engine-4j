package de.uniks.vs.jalica.engine.planselection;

import de.uniks.vs.jalica.engine.RunningPlan;
import de.uniks.vs.jalica.engine.model.AbstractPlan;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by alex on 21.07.17.
 */
public interface IPlanSelector {
    ArrayList<RunningPlan> getPlansForState(RunningPlan rp, ArrayList<AbstractPlan> plans, Vector<Long> agents);
    RunningPlan getBestSimilarAssignment(RunningPlan rp, Vector<Long> agents);

}
