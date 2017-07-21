package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.unknown.Plan;
import de.uniks.vs.jalica.unknown.PlanningProblem;

/**
 * Created by alex on 21.07.17.
 */
public interface IPlanner {
    Plan requestPlan(PlanningProblem pp);
}
