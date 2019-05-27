package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.engine.model.BehaviourConfiguration;

import java.util.HashMap;

/**
 * Created by alex on 17.07.17.
 */
public interface IBehaviourPool {
    void stopBehaviour(RunningPlan runningPlan);
    void startBehaviour(RunningPlan runningPlan);
    boolean init(IBehaviourCreator bc);

    /**
     * Stops this engine module
     */
     void stopAll();

    /**
     * Returns a map of all available de.uniks.vs.jalica.autogenerated.behaviours
     */
     HashMap<BehaviourConfiguration, BasicBehaviour> getAvailableBehaviours();
}
