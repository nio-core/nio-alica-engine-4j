package de.uniks.vs.jalica.unknown;

/**
 * Created by alex on 17.07.17.
 */
public interface IBehaviourPool {
    void stopBehaviour(AbstractPlan plan);

    void startBehaviour(RunningPlan runningPlan);
}
