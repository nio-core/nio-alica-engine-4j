package de.uniks.vs.jalica.autogenerated;

import de.uniks.vs.jalica.autogenerated.plans.behaviour.*;
import de.uniks.vs.jalica.engine.BasicBehaviour;
import de.uniks.vs.jalica.engine.IBehaviourCreator;
import de.uniks.vs.jalica.engine.AlicaEngine;

public class BehaviourCreator implements IBehaviourCreator {

    @Override
    public BasicBehaviour createBehaviour(Long behaviourConfId, AlicaEngine ae) {

//        if (behaviourConfId == 1428508367402l) {
//            return new TriggerC(ae);
//        }
//
//        if (behaviourConfId == 1402488763903l) {
//            return new DefendMid(ae);
//        }
//
//        if (behaviourConfId == 1479556115746l) {
//            return new QueryBehaviour1(ae);
//        }
//
//        if (behaviourConfId == 1429017293301l) {
//            return new NotToTrigger(ae);
//        }

        if (behaviourConfId == 1402489366699l) {
            return new AttackOpp(ae);
        }

//        if (behaviourConfId == 1428508312886l) {
//            return new TriggerA(ae);
//        }

        if (behaviourConfId == 1402488866727l) {
            return new Attack(ae);
        }

        if (behaviourConfId == 1402488712657l) {
            return new MidFieldStandard(ae);
        }

        if (behaviourConfId == 1402488763903l) {
            return new DefendMid(ae);
        }

        if (behaviourConfId == 1402488956661l) {
            return new Tackle(ae);
        }


        else {
//            default:
            System.err.println( "BehaviourCreator: Unknown behaviour requested: " + behaviourConfId );
            try {
                throw new Exception();
            } catch (Exception e) {
                e.printStackTrace();
            }
//                break;
            return null;
        }
    }

}
