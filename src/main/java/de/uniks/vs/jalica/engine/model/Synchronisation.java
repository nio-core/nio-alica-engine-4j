package de.uniks.vs.jalica.engine.model;

import de.uniks.vs.jalica.engine.common.CommonUtils;
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
 */
public class Synchronisation {

    AlicaEngine ae;
    SyncModule syncModul;
    SyncTransition syncTransition;
    int myID;
    long lastTalkTime;
    SyncData lastTalkData;
    long syncStartTime;
    boolean readyForSync;
    long lastTick;
    ArrayList<SyncReady> receivedSyncReadys;
    ArrayList<Long> connectedTransitions;
    RunningPlan runningPlan;
    ArrayList<SyncRow> rowsOK;
    SyncRow myRow;
    ArrayList<SyncRow> syncMatrix;

    public boolean isValid(long curTick) {
        boolean stillActive = (this.lastTick + 2 >= curTick);

        if (!stillActive) {
            //notify others if i am part of the synchronisation already (i.e. have an own row)

            if (myRow != null) {

                if (myRow.getSyncData() != null) {
                    myRow.getSyncData().conditionHolds = false;
                    sendTalk(myRow.getSyncData());
                }
            }
            return false;
        }
        long now = (long) ae.getAlicaClock().now().time / 1000000L;

        if (this.lastTalkTime != 0) {  //talked already

//#ifdef SM_FAILURE
            if (CommonUtils.SM_FAILURE_debug) System.out.println("S: TestTimeOut on Sync: " + this.syncTransition.getID() );
//#endif
            if ((now > this.syncTransition.getTalkTimeOut() + this.lastTalkTime) && !this.readyForSync) {

                if (this.myRow != null) {
                    sendTalk(this.myRow.getSyncData());
                }
            }
        }

//#ifdef SM_FAILURE
        if (CommonUtils.SM_FAILURE_debug) System.out.println("S: TestTimeOut(): syncStarTime " + this.syncStartTime);
//#endif

        if (this.syncTransition.isFailOnSyncTimeOut()) {

            if (now > this.syncTransition.getSyncTimeOut() + this.syncStartTime) {
//#ifdef SM_FAILURE
                if (CommonUtils.SM_FAILURE_debug) System.out.println("S: TestTimeOut() sync failed" );
//#endif
                return false;
            }
        }
        return true;
    }

    private void sendTalk(SyncData sd) {
        SyncTalk talk = new SyncTalk();
        talk.syncData.add(sd);
        this.lastTalkTime = (long) ae.getAlicaClock().now().time / 1000000L;
//#ifdef SM_MESSAGES
        if (CommonUtils.SM_MESSAGES_debug) System.out.println("S: Sending Talk TID: " + sd.transitionID );
//#endif
        this.syncModul.sendSyncTalk(talk);
    }

    public SyncTransition getSyncTransition() {return syncTransition;}
}
