package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.engine.model.Plan;
import de.uniks.vs.jalica.engine.common.PlanningProblem;

/**
 * Created by alex on 21.07.17.
 */
@Deprecated
public interface IPlanner {
    Plan requestPlan(PlanningProblem pp);
}
