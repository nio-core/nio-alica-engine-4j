package de.uniks.vs.jalica.engine.containers.messages;

import de.uniks.vs.jalica.engine.containers.EntryPointAgents;
import de.uniks.vs.jalica.engine.containers.Message;

import java.util.LinkedList;

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
