package de.uniks.vs.jalica.engine.common;

import de.uniks.vs.jalica.engine.AlicaTime;

import java.util.Vector;

public class VarValue {

    public long id;
    public Vector<Integer> val;
    public AlicaTime lastUpdate;

    public VarValue(long vid, Vector<Integer> v, AlicaTime time) {
        this.id = vid;
        this.val = v;
        this.lastUpdate = time;
    }
}
