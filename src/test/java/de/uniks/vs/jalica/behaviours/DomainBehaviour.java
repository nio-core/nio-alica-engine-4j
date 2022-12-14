package de.uniks.vs.jalica.behaviours;

import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.BasicBehaviour;
import de.uniks.vs.jalica.engine.common.SystemConfig;

/**
 * Created by alex on 01.08.17.
 */
public abstract class DomainBehaviour extends BasicBehaviour {

    protected String agentName;
    protected SystemConfig systemConfig;
//    protected double maxTranslation;
    protected int ownID;

    public DomainBehaviour(String name, AlicaEngine ae) {
        super(name);
        this.systemConfig = ae.getSystemConfig();
        this.agentName = ae.getAgentName();
        this.ownID = systemConfig.getOwnAgentID();
    }

    public String getAgentName() {
        return agentName;
    }
}
