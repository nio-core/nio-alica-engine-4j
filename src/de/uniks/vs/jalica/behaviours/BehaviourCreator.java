package de.uniks.vs.jalica.behaviours;

import de.uniks.vs.jalica.behaviours.helloworld.Publish;
import de.uniks.vs.jalica.behaviours.helloworld.Subscribe;
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

        if (behaviourConfId == 1528125256074l) {
            return new Subscribe(ae);
        }

        if (behaviourConfId == 1528125242157l) {
            return new Publish(ae);
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

