package de.uniks.vs.jalica.engine.model;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.AlicaTime;
import de.uniks.vs.jalica.engine.common.SyncData;
import de.uniks.vs.jalica.engine.common.SyncTransition;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.RunningPlan;
import de.uniks.vs.jalica.engine.containers.SyncReady;
import de.uniks.vs.jalica.engine.syncmodule.SyncRow;
import de.uniks.vs.jalica.engine.containers.SyncTalk;
import de.uniks.vs.jalica.engine.syncmodule.SyncModule;

import java.util.ArrayList;

/**
 * Created by alex on 11.11.17.
 * update 22.6.19
 */
public class Synchronisation extends AlicaElement {

    private ArrayList<Transition> inSync;
    private Plan plan;

    private AlicaTime talkTimeout;
    private AlicaTime syncTimeout;

    private boolean failOnSyncTimeout;

    public boolean isFailOnSyncTimeOut() {
        return this.failOnSyncTimeout;
    }

    public AlicaTime getSyncTimeOut() {
        return this.syncTimeout;
    }

    public AlicaTime getTalkTimeOut() {
        return this.talkTimeout;
    }

    public Plan getPlan() {
        return this.plan;
    }

    public ArrayList<Transition> getInSync() {
        return this.inSync;
    }


    public Synchronisation() {
        this.failOnSyncTimeout = false;
        this.syncTimeout = new AlicaTime().inMilliseconds(3000);
        this.talkTimeout = new AlicaTime().inMilliseconds(30);
        this.plan = null;
    }


    public String toString() {
        String indent = "";
        String ss = "";
        ss += indent + "#Synchronisation: " + getName() + " " + getID() + "\n";
        if (this.plan != null) {
            ss += indent + "\t Plan: " + this.plan.getID() + " " + this.plan.getName() + "\n";
        }
        ss += "\n";
        ss += indent + "\t TalkTimeOut: " + this.talkTimeout.inMilliseconds() + "\n";
        ss += indent + "\t SyncTimeOut: " + this.syncTimeout.inMilliseconds() + "\n";
        ss += indent + "\t FailOnSyncTimeOut: " + this.failOnSyncTimeout + "\n";
        ss += indent + "\t InSync: " + this.inSync.size() + "\n";
        for (Transition t : this.inSync) {
            ss += indent + "\t" + t.getID() + " " + t.getName() + "\n";
        }
        ss += "\n";
        ss += "#EndSynchronisation" + "\n";
        return ss;
    }

    public void setFailOnSyncTimeOut(boolean failOnSyncTimeOut) {
        this.failOnSyncTimeout = failOnSyncTimeOut;
    }

    public void setSyncTimeOut(AlicaTime syncTimeOut) {
        this.syncTimeout = syncTimeOut;
    }

    public void setTalkTimeOut(AlicaTime talkTimeOut) {
        this.talkTimeout = talkTimeOut;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public void setInSync(ArrayList<Transition> inSync) {
        this.inSync = inSync;
    }
}
