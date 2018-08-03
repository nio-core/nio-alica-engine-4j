package de.uniks.vs.jalica.unknown;

import java.util.Vector;

public class VarValue {

    long id;
    Vector<Integer> val;
    double lastUpdate;

    VarValue(long vid, Vector<Integer> v, double now)
    {
        this.id = vid;
        this.val = v;
        this.lastUpdate = now;
    }
}
