package de.uniks.vs.jalica.unknown;

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
        String info = "\n Sender               :" +senderID + " \n";
        info += " States               :"+stateIDs+ " \n";
        info += " succeeded EntryPoints:"+succeededEPs;
        return info;
    }
}

