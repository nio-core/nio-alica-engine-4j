package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.BasicBehaviour;

/**
 * Created by alex on 14.07.17.
 */
public interface IBehaviourCreator {
    public BasicBehaviour createBehaviour(long key, Object context);
}
