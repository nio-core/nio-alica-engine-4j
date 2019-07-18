package de.uniks.vs.jalica.engine.teammanagement.view.iterator;

import de.uniks.vs.jalica.engine.idmanagement.ID;
import de.uniks.vs.jalica.engine.teammanagement.Agent;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ActiveAgentBaseIterator {

    Iterator it;
    LinkedHashMap<ID, Agent> map;


    public ActiveAgentBaseIterator(Iterator it, LinkedHashMap<ID, Agent> map){
         this.it = it;
         this.map = map;
        toNextValid();
    }

    // operator++
    public ActiveAgentBaseIterator increase(){
//       it.next();
        toNextValid();
        return this;
    }

    // operator==
    public boolean equals ( ActiveAgentBaseIterator o)  { return this.it == o.it; }

    // operator!=
    public boolean unequal ( ActiveAgentBaseIterator o)  { return !(this == o); }

    protected void toNextValid() {

        while (this.it.hasNext()) {
            Map.Entry<ID, Agent> next = (Map.Entry<ID, Agent>) this.it.next();

            if (next.getValue().isActive()) {
                return;
            }
        }
    }
}
