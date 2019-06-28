package de.uniks.vs.jalica.engine.common;

import de.uniks.vs.jalica.engine.AlicaTime;
import de.uniks.vs.jalica.engine.constrainmodule.Variant;

import java.util.ArrayList;
import java.util.Vector;

/**
 * 23.6.19
 */
public class VarValue {

    public Variant val;
    public AlicaTime lastUpdate;

    public VarValue(Variant v, AlicaTime time) {
        this.val = v;
        this.lastUpdate = time;
    }
}
