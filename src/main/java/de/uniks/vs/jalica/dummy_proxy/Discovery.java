package de.uniks.vs.jalica.dummy_proxy;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Discovery {

     protected long ownID;
     protected HashMap<Long, CommunicationNode> commNodes = new HashMap<>();

     protected abstract void init();
}
