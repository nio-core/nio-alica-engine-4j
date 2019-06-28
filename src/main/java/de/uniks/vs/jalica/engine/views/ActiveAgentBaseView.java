package de.uniks.vs.jalica.engine.views;

import de.uniks.vs.jalica.engine.teammanagement.Agent;

import java.util.HashMap;

public class ActiveAgentBaseView {

    protected HashMap<Long, Agent> map;

    public ActiveAgentBaseView(HashMap<Long, Agent> map) {
        this.map = map;
    }
}
