package de.uniks.vs.jalica.engine.common;

import java.util.Vector;

public class VarValue {

    public long id;
    public Vector<Integer> val;
    public double lastUpdate;

    public VarValue(long vid, Vector<Integer> v, double now) {
        this.id = vid;
        this.val = v;
        this.lastUpdate = now;
    }
}
