package de.uniks.vs.jalica.communication.messages;

import de.uniks.vs.jalica.unknown.Message;

import java.util.ArrayList;

/**
 * Created by alex on 10.11.17.
 */
public class PlanTreeInfo implements Message {
    public long senderID;
    public ArrayList<Long> stateIDs = new ArrayList<>();
    public ArrayList<Long> succeededEPs = new ArrayList<>();

    @Override
    public String toString() {
        return  "\n Sender:" +senderID + " | "+ " States:"+stateIDs+ " | " + " succeeded EntryPoints:"+succeededEPs;
    }
}

