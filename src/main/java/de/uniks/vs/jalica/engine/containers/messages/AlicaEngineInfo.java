package de.uniks.vs.jalica.engine.containers.messages;

import de.uniks.vs.jalica.engine.containers.Message;

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

    @Override
    public String toString() {
        return  "\n Sender:" +senderID + " | "
                + " Master Plan:"+masterPlan+ " | "
                + " Current Plan:"+currentPlan+ " | "
                + " Current Task:"+currentTask+ " | "
                + " Current State:"+currentState+ " | "
                + " Current Role:"+currentRole+ " | "
                + " Agents in State:"+agentIDsWithMe;
    }
}
