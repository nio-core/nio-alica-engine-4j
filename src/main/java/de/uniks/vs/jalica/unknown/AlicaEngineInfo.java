package de.uniks.vs.jalica.unknown;

import java.util.Vector;

/**
 * Created by alex on 13.07.17.
 */
public class AlicaEngineInfo implements Message {
    public long senderID;
    public String masterPlan;
    public String currentPlan;
    public Vector<Long> agentIDsWithMe = new Vector<>();
    public String currentTask;
    public String currentState;
    public String currentRole;
}
