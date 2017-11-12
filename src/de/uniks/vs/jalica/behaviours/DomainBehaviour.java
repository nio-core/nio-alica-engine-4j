package de.uniks.vs.jalica.behaviours;

import de.uniks.vs.jalica.supplementary.SystemConfig;

/**
 * Created by alex on 01.08.17.
 */
public class DomainBehaviour extends BasicBehaviour {

    protected SystemConfig sc;

    private double __maxTranslation;
    private int ownID;

    public DomainBehaviour(String name) {
        super(name);
        this.sc = SystemConfig.getInstance();
        this.ownID = sc.getOwnRobotID();
    }
}
