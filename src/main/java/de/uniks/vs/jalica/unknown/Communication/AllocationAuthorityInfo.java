package de.uniks.vs.jalica.unknown.Communication;

import de.uniks.vs.jalica.unknown.EntryPointAgents;
import de.uniks.vs.jalica.unknown.Message;

import java.util.LinkedList;
import java.util.Vector;

/**
 * Created by alex on 27.07.17.
 */
public class AllocationAuthorityInfo implements Message {

    public long senderID;
    public long planID;
    public long parentState;
    public long planType;
    public long authority;

    public LinkedList<EntryPointAgents> entryPointAgents = new LinkedList<>();
}
