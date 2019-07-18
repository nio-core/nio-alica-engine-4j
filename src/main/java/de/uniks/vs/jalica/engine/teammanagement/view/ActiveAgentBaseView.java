package de.uniks.vs.jalica.engine.teammanagement.view;

import de.uniks.vs.jalica.engine.idmanagement.ID;
import de.uniks.vs.jalica.engine.teammanagement.Agent;

import java.util.HashMap;

public class ActiveAgentBaseView {

    protected HashMap<ID, Agent> map;

    public ActiveAgentBaseView(HashMap<ID, Agent> map) {
        this.map = map;
    }
}
