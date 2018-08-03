package de.uniks.vs.jalica.behaviours;

import de.uniks.vs.jalica.engine.AlicaEngine;

/**
 * Created by alex on 14.07.17.
 */
public interface IBehaviourCreator {

    public BasicBehaviour createBehaviour(Long key, AlicaEngine ae);
}
