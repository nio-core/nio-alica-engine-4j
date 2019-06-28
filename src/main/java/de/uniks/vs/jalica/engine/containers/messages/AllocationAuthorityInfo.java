package de.uniks.vs.jalica.engine.containers.messages;

import de.uniks.vs.jalica.engine.containers.EntryPointAgents;
import de.uniks.vs.jalica.engine.containers.Message;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by alex on 27.07.17.
 * updated 21.6.19
 */
public class AllocationAuthorityInfo implements Message {

    public long senderID;
    public long planID;
    public long parentState;
    public long planType;
    public long authority;

    public LinkedList<EntryPointAgents> entryPointAgents = new LinkedList<>();

    //stdAllocationAuthorityInfo -> Tuple
    //Tuple<Long, ArrayList<Long>> stdEntryPointRobot -> Tuple

    public AllocationAuthorityInfo(){
        this.senderID = -1;
        this.planID = 0;
        this.parentState = 0;
        this.planType = 0;
        this.authority = -1;
    }

    public AllocationAuthorityInfo(Tuple s) {
        this.senderID = (long) s.get()[0];
        this.planID = (long) s.get()[1];
        this.parentState = (long) s.get()[2];
        this.planType = (long) s.get()[3];
        this.authority = (long) s.get()[4];
        ArrayList<Tuple> tmp = (ArrayList<Tuple>) s.get()[5];

        for (Tuple e : tmp) {
            this.entryPointAgents.add(new EntryPointAgents(e));
        }
    }


     public Tuple toStandard() {
        ArrayList<Tuple> r = new ArrayList<>();

        for (EntryPointAgents e : entryPointAgents) {
            r.add(e.toStandard());
        }
        return new Tuple(senderID, planID, parentState, planType, authority, r);
    }

    @Override
    public String toString() {
        String o = "";
        o += "AAI sender: " + this.senderID + " plan: " + this.planID + "\n";
        // TODO: complete implementation
//        std::copy(aai.entryPointRobots.begin(), aai.entryPointRobots.end(), std::ostream_iterator<EntryPointRobots>(o, "\n"));
        return o;
    }
}
