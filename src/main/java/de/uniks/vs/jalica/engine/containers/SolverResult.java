package de.uniks.vs.jalica.engine.containers;

import de.uniks.vs.jalica.engine.idmanagement.ID;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by alex on 10.11.17.
 */
public class SolverResult implements Message {
    public ID senderID;
    public ArrayList<SolverVar> vars = new ArrayList<>();
}
