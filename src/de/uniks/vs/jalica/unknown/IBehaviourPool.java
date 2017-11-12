package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.behaviours.BasicBehaviour;
import de.uniks.vs.jalica.behaviours.BehaviourConfiguration;
import de.uniks.vs.jalica.behaviours.IBehaviourCreator;

import java.util.HashMap;

/**
 * Created by alex on 17.07.17.
 */
public interface IBehaviourPool {
    void stopBehaviour(AbstractPlan plan);
    void startBehaviour(RunningPlan runningPlan);
    boolean init(IBehaviourCreator bc);

    /**
     * Stops this engine module
     */
     void stopAll();

    /**
     * Returns a map of all available behaviours
     */
     HashMap<BehaviourConfiguration, BasicBehaviour> getAvailableBehaviours();
}
