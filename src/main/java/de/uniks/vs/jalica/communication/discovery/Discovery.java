package de.uniks.vs.jalica.communication.discovery;

import de.uniks.vs.jalica.communication.NetworkNode;
import de.uniks.vs.jalica.engine.idmanagement.ID;

import java.util.HashMap;

public abstract class Discovery {

     protected ID ownID;
     protected HashMap<Long, NetworkNode> commNodes = new HashMap<>();

     protected abstract void init();
}
