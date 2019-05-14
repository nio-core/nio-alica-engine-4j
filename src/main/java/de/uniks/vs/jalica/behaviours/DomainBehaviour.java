package de.uniks.vs.jalica.behaviours;

import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.supplementary.SystemConfig;

/**
 * Created by alex on 01.08.17.
 */
public abstract class DomainBehaviour extends BasicBehaviour {

    protected SystemConfig systemConfig;
    protected double maxTranslation;
    protected int ownID;

    public DomainBehaviour(String name, AlicaEngine ae) {
        super(name);
        this.systemConfig = ae.getSystemConfig();
        this.ownID = systemConfig.getOwnRobotID();
    }
}