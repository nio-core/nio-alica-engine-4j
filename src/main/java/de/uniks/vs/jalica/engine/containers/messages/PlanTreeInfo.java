package de.uniks.vs.jalica.engine.containers.messages;

import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.containers.Message;
import de.uniks.vs.jalica.engine.idmanagement.ID;

import java.util.ArrayList;

/**
 * Created by alex on 10.11.17.
 */
public class PlanTreeInfo implements Message {

    public ID senderID;
    public ArrayList<Long> stateIDs = new ArrayList<>();
    public ArrayList<Long> succeededEPs = new ArrayList<>();

    public String toString(AlicaEngine ae) {
        String s = " Sender:" +senderID + " | ";

        for (Long state : stateIDs)
               s += " States: "+ (ae.getPlanRepository().getStates().get(state)!= null ? ae.getPlanRepository().getStates().get(state).getName() : state) + " | ";
        s += " succeeded EntryPoints:"+succeededEPs;
        return s;
    }

    @Override
    public String toString() {
        return  " Sender:" +senderID + " | "+ " States:"+stateIDs+ " | " + " succeeded EntryPoints:"+succeededEPs;
    }
}

