package de.uniks.vs.jalica.behaviours;

import de.uniks.vs.jalica.engine.AlicaEngine;

/**
 * Created by alex on 13.07.17.
 */
public class BehaviourCreator implements IBehaviourCreator {

    @Override
    public BasicBehaviour createBehaviour(Long behaviourConfId, AlicaEngine ae) {

        if (behaviourConfId == 1482486281206l) {
                               //1482486281206
            // case 1482486281206:
//            case (long)1482486206:
            return new DummyBehaviour(ae);
//            break;
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

