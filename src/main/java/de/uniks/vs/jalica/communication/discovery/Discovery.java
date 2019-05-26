package de.uniks.vs.jalica.communication.discovery;

import de.uniks.vs.jalica.communication.NetworkNode;

import java.util.HashMap;

public abstract class Discovery {

     protected long ownID;
     protected HashMap<Long, NetworkNode> commNodes = new HashMap<>();

     protected abstract void init();
}
