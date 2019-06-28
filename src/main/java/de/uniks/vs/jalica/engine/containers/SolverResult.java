package de.uniks.vs.jalica.engine.containers;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by alex on 10.11.17.
 */
public class SolverResult implements Message {
    public long senderID;
    public ArrayList<SolverVar> vars = new ArrayList<>();
}
