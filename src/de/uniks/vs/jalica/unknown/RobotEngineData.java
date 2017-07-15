package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.engine.AlicaEngine;

/**
 * Created by alex on 14.07.17.
 */
public class RobotEngineData {
    private boolean active;
    private RobotProperties properties;

    public RobotEngineData(AlicaEngine ae, RobotProperties rp) {

    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public RobotProperties getProperties() {
        return properties;
    }
}
