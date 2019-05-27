package de.uniks.vs.jalica.engine.common;

import de.uniks.vs.jalica.engine.model.AlicaElement;
import de.uniks.vs.jalica.engine.model.Plan;
import de.uniks.vs.jalica.engine.model.Transition;

import java.util.ArrayList;

/**
 * Created by alex on 13.07.17.
 */
public class SyncTransition extends AlicaElement {

    private ArrayList<Transition> inSync = new ArrayList<>();
    private long talkTimeOut;
    private long syncTimeOut;
    private Plan plan;
    private boolean failOnSyncTimeOut;

    public void setTalkTimeOut(long talkTimeOut) {
        this.talkTimeOut = talkTimeOut;
    }

    public void setSyncTimeOut(long syncTimeOut) {
        this.syncTimeOut = syncTimeOut;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public ArrayList<Transition> getInSync() {
        return inSync;
    }

    public long getTalkTimeOut() {return talkTimeOut;}

    public boolean isFailOnSyncTimeOut() {return failOnSyncTimeOut;}

    public long getSyncTimeOut() {return syncTimeOut;}
}
