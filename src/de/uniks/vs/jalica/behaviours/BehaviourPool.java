package de.uniks.vs.jalica.behaviours;

import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.unknown.RunningPlan;

import java.util.HashMap;

/**
 * Created by alex on 13.07.17.
 */
public class BehaviourPool {

    private  HashMap<BehaviourConfiguration, BasicBehaviour> availableBehaviours;
    private  IBehaviourCreator behaviourCreator;
    private  AlicaEngine ae;

    public BehaviourPool(AlicaEngine ae) {
        this.ae = ae;
        this.behaviourCreator = null;
        this.availableBehaviours = new HashMap<BehaviourConfiguration, BasicBehaviour >();
    }

    public boolean init(BehaviourCreator bc) {
        if (this.behaviourCreator != null)
        {
            this.behaviourCreator = null;
        }

        this.behaviourCreator = bc;

        HashMap<Long, BehaviourConfiguration> behaviourConfs = ae.getPlanRepository().getBehaviourConfigurations();
        for (Long key : behaviourConfs.keySet())
        {
            BasicBehaviour basicBeh = (BasicBehaviour)this.behaviourCreator.createBehaviour(key);
            if (basicBeh != null)
            {
                // set stuff from behaviour configuration in basic behaviour object
                basicBeh.setParameters(behaviourConfs.get(key).getParameters());
                basicBeh.setVariables(behaviourConfs.get(key).getVariables());
                basicBeh.setDelayedStart(behaviourConfs.get(key).getDeferring());
                basicBeh.setInterval(1000 / behaviourConfs.get(key).getFrequency());

                this.availableBehaviours.put(behaviourConfs.get(key), basicBeh);
            }
            else
            {
                return false;
            }
        }
        return true;
    }

    void startBehaviour(RunningPlan rp) {}
    void stopBehaviour(RunningPlan rp) {}
    void stopAll() {}
}
