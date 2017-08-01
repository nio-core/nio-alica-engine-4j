package de.uniks.vs.jalica.behaviours;

/**
 * Created by alex on 13.07.17.
 */
public class BehaviourCreator implements IBehaviourCreator {

    @Override
    public BasicBehaviour createBehaviour(Long behaviourConfId) {

        if (behaviourConfId == 1482486206) {
            // case 1482486281206:
//            case (long)1482486206:
            return new DummyBehaviour();
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

