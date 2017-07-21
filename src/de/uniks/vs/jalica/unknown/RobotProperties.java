package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.engine.AlicaEngine;

import java.util.HashMap;

/**
 * Created by alex on 14.07.17.
 */
public class RobotProperties {
    private int id;
    private HashMap<String, Characteristic> characteristics;
    private String name;

    public RobotProperties(AlicaEngine ae, String cHar) {

    }

    public int getId() {
        return id;
    }

    public HashMap<String, Characteristic> getCharacteristics() {
        return characteristics;
    }

    public String getName() {
        return name;
    }
}
