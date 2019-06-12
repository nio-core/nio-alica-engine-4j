package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.model.BehaviourConfiguration;

import java.util.HashMap;

/**
 * Created by alex on 13.07.17.
 */
public class BehaviourPool implements IBehaviourPool {

    private  HashMap<BehaviourConfiguration, BasicBehaviour> availableBehaviours;
    private IBehaviourCreator behaviourCreator;
    private  AlicaEngine ae;

    public BehaviourPool(AlicaEngine ae) {
        this.ae = ae;
        this.behaviourCreator = null;
        this.availableBehaviours = new HashMap<BehaviourConfiguration, BasicBehaviour >();
    }

//    @Override
//    public void stopBehaviour(RunningPlan runningPlan) {
//        BehaviourConfiguration bc = (BehaviourConfiguration) runningPlan.getPlan();
//
//        if (bc != null) {
//            BasicBehaviour bb = this.availableBehaviours.get(bc);
//
//            if (bb != null) {
//                bb.stop();
//            }
//        }
//        else {
//            System.out.println("BP::stopBehaviour(): Cannot stop Behaviour of given RunningPlan! Plan Name: " + runningPlan.getPlan().getName() + " Plan Id: " + runningPlan.getPlan().extractID() +"\n");
//        }
//    }

    public void startBehaviour(RunningPlan runningPlan) {
        BehaviourConfiguration behaviourConfiguration = (BehaviourConfiguration)(runningPlan.getPlan());

        if (behaviourConfiguration != null) {
            if (CommonUtils.BP_DEBUG_debug) System.out.println("BP::  BehaviourPool:" + this.availableBehaviours.size() );
            BasicBehaviour behaviour = this.availableBehaviours.get(behaviourConfiguration);

            if (behaviour != null) {
                runningPlan.setBasicBehaviour(behaviour);
                behaviour.setRunningPlan(runningPlan);

                behaviour.start();
            }
        }
        else {
            System.err.println("BP::startBehaviour(): Cannot start Behaviour of given RunningPlan! Plan Name: " + runningPlan.getPlan().getName() + " Plan Id: " + runningPlan.getPlan().getID() );
        }
    }

    @Override
    public boolean init(IBehaviourCreator bc) {

        this.behaviourCreator = bc;

        HashMap<Long, BehaviourConfiguration> behaviourConfs = ae.getPlanRepository().getBehaviourConfigurations();

        for (long key : behaviourConfs.keySet()) {
            BasicBehaviour domainImplementation = this.behaviourCreator.createBehaviour(behaviourConfs.get(key).getBehaviour().getID(), ae);

            if (domainImplementation != null) {
                BehaviourConfiguration configuration = behaviourConfs.get(key);
                // set stuff from behaviour configuration in basic behaviour object
                domainImplementation.setParameters(configuration.getParameters());
                domainImplementation.setVariables(configuration.getVariables());
                domainImplementation.setDelayedStart(configuration.getDeferring());
                //TODO: HACK
                domainImplementation.setInterval(1000 / 1000 + configuration.getFrequency());

                this.availableBehaviours.put(configuration, domainImplementation);
                configuration.getBehaviour().setImplementation(domainImplementation);
            }
            else
            {
                return false;
            }
        }
        return true;
    }

//    @Override
//    public boolean init(IBehaviourCreator bc) {
//
//        if (this.behaviourCreator != null) {
//            this.behaviourCreator = null;
//        }
//        this.behaviourCreator = bc;
//        HashMap<Long, BehaviourConfiguration> behaviourConfs = ae.getPlanRepository().getBehaviourConfigurations();
//
//        for (Long key : behaviourConfs.keySet()) {
//            BasicBehaviour basicBeh = (BasicBehaviour)this.behaviourCreator.createBehaviour(key, ae);
//
//            if (basicBeh != null) {
//                // set stuff from behaviour configuration in basic behaviour object
//                basicBeh.setParameters(behaviourConfs.get(key).getParameters());
//                basicBeh.setVariables(behaviourConfs.get(key).getVariables());
//                basicBeh.setDelayedStart(behaviourConfs.get(key).getDeferring());
//                basicBeh.setInterval(1000 / behaviourConfs.get(key).getFrequency());
//
//                this.availableBehaviours.put(behaviourConfs.get(key), basicBeh);
//                behaviourConfs.get(key).getBehaviour().setImplementation(basicBeh);
//            }
//            else {
//                return false;
//            }
//        }
//        return true;
//    }

    public void stopBehaviour(RunningPlan rp) {
        BehaviourConfiguration behaviourConfiguration = (BehaviourConfiguration)(rp.getPlan());

        if (behaviourConfiguration != null) {
            BasicBehaviour basicBehaviour = this.availableBehaviours.get(behaviourConfiguration);

            if (basicBehaviour != null) {
                basicBehaviour.stop();
            }
        }
        else {
            System.err.println("BP::stopBehaviour(): Cannot stop Behaviour of given RunningPlan! Plan Name: " + rp.getPlan().getName() + " Plan Id: " + rp.getPlan().getID() );
        }
    }

    public void stopAll() {
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

    @Override
    public HashMap<BehaviourConfiguration, BasicBehaviour> getAvailableBehaviours() {
        return availableBehaviours;
    }
}
