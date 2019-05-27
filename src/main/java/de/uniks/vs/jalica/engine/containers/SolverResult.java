package de.uniks.vs.jalica.engine.containers;

import java.util.Vector;

/**
 * Created by alex on 10.11.17.
 */
public class SolverResult implements Message {
    public long senderID;
    public Vector<SolverVar> vars = new Vector<>();
}
