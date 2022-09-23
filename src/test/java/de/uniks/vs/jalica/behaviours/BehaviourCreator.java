package de.uniks.vs.jalica.behaviours;

import de.uniks.vs.jalica.behaviours.helloworld.Publish;
import de.uniks.vs.jalica.behaviours.helloworld.Subscribe;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.BasicBehaviour;
import de.uniks.vs.jalica.engine.IBehaviourCreator;

/**
 * Created by alex on 13.07.17.
 */
public class BehaviourCreator implements IBehaviourCreator {

    @Override
    public BasicBehaviour createBehaviour(long behaviourConfId, Object context) {

        if (behaviourConfId == 1482486281206l) {
                               //1482486281206
            // case 1482486281206:
//            case (long)1482486206:
            return new DummyBehaviour(context);
//            break;
        }

        else if (behaviourConfId == 1528125256074l) {
            return new Subscribe(context);
        }

        else if (behaviourConfId == 1528125242157l) {
            return new Publish(context);
        }

        else {
//            default:
                System.err.println( "BehaviourCreator: Unknown behaviour requested: " + behaviourConfId +".behaviour" );
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

