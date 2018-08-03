package de.uniks.vs.jalica.unknown;

import java.util.Vector;

/**
 * Created by alex on 27.07.17.
 */
public class AllocationAuthorityInfo implements Message {
    public Vector<EntryPointAgents> entryPointAgents;
    public long planId;
    public long planType;
    public long senderID;
    public long parentState;
    public long authority;
}
