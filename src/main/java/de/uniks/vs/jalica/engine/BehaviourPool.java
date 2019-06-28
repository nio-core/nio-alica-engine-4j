package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.model.Behaviour;
import de.uniks.vs.jalica.engine.model.BehaviourConfiguration;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by alex on 13.07.17.
 * update 21.6.19
 */
public class BehaviourPool implements IBehaviourPool {

    private  HashMap<Behaviour, BasicBehaviour> availableBehaviours;
    private IBehaviourCreator behaviourCreator;
    private  AlicaEngine ae;

    public BehaviourPool(AlicaEngine ae) {
        this.ae = ae;
        this.behaviourCreator = null;
        this.availableBehaviours = new HashMap<>();
    }


    public boolean init(IBehaviourCreator bc) {
        this.behaviourCreator = bc;
        LinkedHashMap<Long, Behaviour> behaviours = this.ae.getPlanRepository().getBehaviours();

        for ( Behaviour beh : behaviours.values()) {
            BasicBehaviour basicBeh = this.behaviourCreator.createBehaviour(beh.getID(), ae);

            if (basicBeh != null) {
                // set stuff from behaviour configuration in basic behaviour object
                basicBeh.setBehaviour(beh);
                basicBeh.setDelayedStart(beh.getDeferring());
                basicBeh.setInterval(1000 / (beh.getFrequency() < 1 ? 1 : beh.getFrequency()));
                basicBeh.setEngine(ae);
                basicBeh.init();
                this.availableBehaviours.put(beh, basicBeh);
            } else {
                return false;
            }
        }
        return true;
    }


    public void stopAll() {
        for (Map.Entry<Behaviour, BasicBehaviour> beh_pair : this.availableBehaviours.entrySet()) {
            beh_pair.getValue().stop();
        }
    }


    public void terminateAll() {
        for (Map.Entry<Behaviour, BasicBehaviour> beh_pair : this.availableBehaviours.entrySet()) {
            beh_pair.getValue().terminate();
        }
    }


    public void   startBehaviour(RunningPlan   rp) {
        if (rp.getActivePlan() instanceof Behaviour) {

            Behaviour beh = (Behaviour) rp.getActivePlan();
            BasicBehaviour bb = this.availableBehaviours.get(beh);
            if (bb != null) {
                // set both directions rp <. bb
                rp.setBasicBehaviour(bb);
                bb.setRunningPlan(rp);

                bb.start();
            }
        } else {
            CommonUtils.aboutError("BP::startBehaviour(): Cannot start Behaviour of given RunningPlan! Plan Name: " + rp.getActivePlan().getName()
                    + " Plan Id: " + rp.getActivePlan().getID());
        }
    }

    public void stopBehaviour(RunningPlan rp) {
        if (rp.getActivePlan() instanceof Behaviour) {
            Behaviour beh = (Behaviour) rp.getActivePlan();
            BasicBehaviour bb = this.availableBehaviours.get(beh);
            if (bb != null) {
                bb.stop();
            }
        } else {
            CommonUtils.aboutError("BP::stopBehaviour(): Cannot stop Behaviour of given RunningPlan! Plan Name: " + rp.getActivePlan().getName()
                    + " Plan Id: " + rp.getActivePlan().getID());
        }
    }

    public boolean isBehaviourRunningInContext(RunningPlan rp) {
        if (rp.getActivePlan() instanceof Behaviour) {
            Behaviour beh = (Behaviour) rp.getActivePlan();
            BasicBehaviour bb = this.availableBehaviours.get(beh);
            if (bb != null) {
                return bb.isRunningInContext(rp);
            }
        }
        return false;
    }

    public HashMap<Behaviour, BasicBehaviour> getAvailableBehaviours()  { return this.availableBehaviours; }

//    public boolean isBehaviourRunningInContext(RunningPlan rp) {
//        if (rp.getActivePlan() instanceof Behaviour) {
//            Behaviour beh = (Behaviour) rp.getActivePlan();
//            BasicBehaviour bb = this.availableBehaviours.get(beh);
//
//            if (bb != null) {
//                return bb.isRunningInContext(rp);
//            }
//        }
//        return false;
//    }
//
//
//    public void startBehaviour(RunningPlan runningPlan) {
//        BehaviourConfiguration behaviourConfiguration = (BehaviourConfiguration)(runningPlan.getPlan());
//
//        if (behaviourConfiguration != null) {
//            if (CommonUtils.BP_DEBUG_debug) System.out.println("BP::  BehaviourPool:" + this.availableBehaviours.size() );
//            BasicBehaviour behaviour = this.availableBehaviours.get(behaviourConfiguration);
//
//            if (behaviour != null) {
//                runningPlan.setBasicBehaviour(behaviour);
//                behaviour.setRunningPlan(runningPlan);
//
//                behaviour.start();
//            }
//        }
//        else {
//            System.err.println("BP::startBehaviour(): Cannot start Behaviour of given RunningPlan! Plan Name: " + runningPlan.getPlan().getName() + " Plan Id: " + runningPlan.getPlan().getID() );
//        }
//    }
//
//    @Override
//    public boolean init(IBehaviourCreator bc) {
//
//        this.behaviourCreator = bc;
//
//        HashMap<Long, BehaviourConfiguration> behaviourConfs = ae.getPlanRepository().getBehaviourConfigurations();
//
//        for (long key : behaviourConfs.keySet()) {
//            BasicBehaviour domainImplementation = this.behaviourCreator.createBehaviour(behaviourConfs.get(key).getBehaviour().getID(), ae);
//
//            if (domainImplementation != null) {
//                BehaviourConfiguration configuration = behaviourConfs.get(key);
//                // set stuff from behaviour configuration in basic behaviour object
//                domainImplementation.setParameters(configuration.getParameters());
//                domainImplementation.setVariables(configuration.getVariables());
//                domainImplementation.setDelayedStart(configuration.getDeferring());
//                //TODO: HACK
//                domainImplementation.setInterval(1000 / 1000 + configuration.getFrequency());
//
//                this.availableBehaviours.put(configuration, domainImplementation);
//                configuration.getBehaviour().setImplementation(domainImplementation);
//            }
//            else
//            {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    public void stopBehaviour(RunningPlan rp) {
//        BehaviourConfiguration behaviourConfiguration = (BehaviourConfiguration)(rp.getPlan());
//
//        if (behaviourConfiguration != null) {
//            BasicBehaviour basicBehaviour = this.availableBehaviours.get(behaviourConfiguration);
//
//            if (basicBehaviour != null) {
//                basicBehaviour.stop();
//            }
//        }
//        else {
//            System.err.println("BP::stopBehaviour(): Cannot stop Behaviour of given RunningPlan! Plan Name: " + rp.getPlan().getName() + " Plan Id: " + rp.getPlan().getID() );
//        }
//    }
//
//    public void stopAll() {
//        HashMap<Long, BehaviourConfiguration> behaviourConfs = ae.getPlanRepository().getBehaviourConfigurations();
//        for ( BehaviourConfiguration configuration : behaviourConfs.values())
//        {
//            BasicBehaviour bbPtr = this.availableBehaviours.get(configuration);
//            if (bbPtr == null)
//            {
//                System.err.println("BP::stop(): Found Behaviour without an BasicBehaviour attached!" );
//                continue;
//            }
//
//            bbPtr.stop();
//        }
//    }
//
//    @Override
//    public HashMap<BehaviourConfiguration, BasicBehaviour> getAvailableBehaviours() {
//        return availableBehaviours;
//    }
}
