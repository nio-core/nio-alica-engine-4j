package de.uniks.vs.jalica.unknown;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 13.07.17.
 */
public class SyncTransition extends AlicaElement {

    private ArrayList<Transition> inSync = new ArrayList<>();
    private long talkTimeOut;
    private long syncTimeOut;
    private Plan plan;

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
}
