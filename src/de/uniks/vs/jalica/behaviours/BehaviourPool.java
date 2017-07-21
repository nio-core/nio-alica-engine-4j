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
        if (this.behaviourCreator != null) {
            this.behaviourCreator = null;
        }

        this.behaviourCreator = bc;

        HashMap<Long, BehaviourConfiguration> behaviourConfs = ae.getPlanRepository().getBehaviourConfigurations();
        for (Long key : behaviourConfs.keySet()) {
            BasicBehaviour basicBeh = (BasicBehaviour)this.behaviourCreator.createBehaviour(key);
            if (basicBeh != null) {
                // set stuff from behaviour configuration in basic behaviour object
                basicBeh.setParameters(behaviourConfs.get(key).getParameters());
                basicBeh.setVariables(behaviourConfs.get(key).getVariables());
                basicBeh.setDelayedStart(behaviourConfs.get(key).getDeferring());
                basicBeh.setInterval(1000 / behaviourConfs.get(key).getFrequency());

                this.availableBehaviours.put(behaviourConfs.get(key), basicBeh);
            }
            else {
                return false;
            }
        }
        return true;
    }

    void startBehaviour(RunningPlan rp) {

        BehaviourConfiguration bc = (BehaviourConfiguration)(rp.getPlan());

        if (bc != null)
        {
            BasicBehaviour bb = this.availableBehaviours.get(bc);
            if (bb != null) {
                // set both directions rp <-> bb
                rp.setBasicBehaviour(bb);
                bb.setRunningPlan(rp);

                bb.start();
            }
        }
        else
        {
            System.err.println("BP::startBehaviour(): Cannot start Behaviour of given RunningPlan! Plan Name: " + rp.getPlan().getName() + " Plan Id: " + rp.getPlan().getId() );
        }
    }

    void stopBehaviour(RunningPlan rp) {
        BehaviourConfiguration bc = (BehaviourConfiguration)(rp.getPlan());

        if (bc != null) {
            BasicBehaviour bb = this.availableBehaviours.get(bc);
            if (bb != null)
            {
                bb.stop();
            }
        }
        else
        {
            System.err.println("BP::stopBehaviour(): Cannot stop Behaviour of given RunningPlan! Plan Name: " + rp.getPlan().getName() + " Plan Id: " + rp.getPlan().getId() );
        }
    }

    void stopAll() {
        HashMap<Long, BehaviourConfiguration> behaviourConfs = ae.getPlanRepository().getBehaviourConfigurations();
        for ( BehaviourConfiguration configuration : behaviourConfs.values())
        {
            BasicBehaviour bbPtr = this.availableBehaviours.get(configuration);
            if (bbPtr == null)
            {
                System.err.println("BP::stop(): Found Behaviour without an BasicBehaviour attached!" );
                continue;
            }

            bbPtr.stop();
        }
    }
}
