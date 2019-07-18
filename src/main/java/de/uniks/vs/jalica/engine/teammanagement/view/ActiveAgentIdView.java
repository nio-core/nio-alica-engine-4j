package de.uniks.vs.jalica.engine.teammanagement.view;

import de.uniks.vs.jalica.engine.idmanagement.ID;
import de.uniks.vs.jalica.engine.teammanagement.Agent;

import java.util.ArrayList;
import java.util.HashMap;

public class ActiveAgentIdView extends ActiveAgentBaseView {

    public ActiveAgentIdView(HashMap<ID, Agent> map) {
        super(map);
    }

    public ArrayList<ID> get() {
        ArrayList<ID> agentIDs = new ArrayList<>();

         for(Agent agent  : map.values()) {

             if (agent.isActive()) {
                agentIDs.add(agent.getId());
            }
        }
         return agentIDs;
    }
    int size()  { return get().size(); }
    boolean empty()  { return get().isEmpty(); }

}
