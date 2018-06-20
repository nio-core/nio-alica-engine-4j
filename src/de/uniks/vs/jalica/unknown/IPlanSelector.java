package de.uniks.vs.jalica.unknown;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by alex on 21.07.17.
 */
public interface IPlanSelector {
    ArrayList<RunningPlan> getPlansForState(RunningPlan rp, ArrayList<AbstractPlan> plans, Vector<Integer> agents);
    RunningPlan getBestSimilarAssignment(RunningPlan rp, Vector<Integer> agents);

}
