package de.uniks.vs.jalica.engine.syncmodule;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.AlicaTime;
import de.uniks.vs.jalica.engine.RunningPlan;
import de.uniks.vs.jalica.engine.common.SyncData;
import de.uniks.vs.jalica.engine.containers.SyncReady;
import de.uniks.vs.jalica.engine.containers.SyncTalk;
import de.uniks.vs.jalica.engine.idmanagement.ID;
import de.uniks.vs.jalica.engine.model.Synchronisation;
import de.uniks.vs.jalica.engine.model.Transition;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;

/**
 * updated 22.6.19
 */

public class SynchronisationProcess {

    AlicaEngine ae;
    Lock syncMutex;
    Lock rowOkMutex;
    SyncModule syncModul;
    Synchronisation synchronisation;
    ID myID;
    SyncData lastTalkData;
    AlicaTime lastTalkTime;
    AlicaTime syncStartTime;
    boolean readyForSync;
    long lastTick;
    ArrayList<SyncReady> receivedSyncReadys;
    ArrayList<Long> connectedTransitions;
    RunningPlan runningPlan;
    ArrayList<SyncRow> rowsOK;
    ArrayList<SyncRow> syncMatrix;
    SyncRow myRow;


    public SynchronisationProcess(AlicaEngine ae) {
        this.connectedTransitions = new ArrayList<>();
        this.receivedSyncReadys = new ArrayList<>();
        this.rowsOK = new ArrayList<>();
        this.syncMatrix = new ArrayList<>();
        myID = null;
        this.ae = ae;
        this.syncModul = null;
        this.synchronisation = null;
        this.readyForSync = false;
        this.lastTick = 0;
        this.runningPlan = null;
        this.myRow = null;
        this.lastTalkData = null;
    }

    public SynchronisationProcess(AlicaEngine ae, ID myID,  Synchronisation sync, SyncModule sm) {
        this.connectedTransitions = new ArrayList<>();
        this.receivedSyncReadys = new ArrayList<>();
        this.rowsOK = new ArrayList<>();
        this.syncMatrix = new ArrayList<>();
        this.ae = ae;
        this.synchronisation = sync;
        this.myID = myID;
        this.syncStartTime = ae.getAlicaClock().now();

        for (Transition t : sync.getInSync()) {
            connectedTransitions.add(t.getID());
        }
        this.syncModul = sm;
        this.runningPlan = null;
        this.myRow = null;
        this.readyForSync = false;
        this.lastTick = 0;
        this.lastTalkData = null;
    }

    Synchronisation getSynchronisation() {
        return synchronisation;
    }

    void setTick(long now) {
        this.lastTick = now;
    }

    void changeOwnData(long transitionID, boolean conditionHolds) {

        if (CommonUtils.SP_DEBUG_debug) {
            System.out.println("CHOD: ElapsedTime: " + (ae.getAlicaClock().now().time - this.syncStartTime.time));
        }

        if (!conditionHolds) {
            // my condition does not hold => not ready for syncing
            this.readyForSync = false;
        }
        SyncData sd = new SyncData();
        sd.agentID = this.myID;
        sd.transitionID = transitionID;
        sd.conditionHolds = conditionHolds;
        sd.ack = false;

        boolean maySendTalk = true;

        {
//            std::lock_guard<mutex> lock(syncMutex);
            synchronized (this) {
                if (myRow != null) {
                    if (/*sd->ack != myRow->getSyncData()->ack
                                                ||*/
                            sd.conditionHolds != myRow.getSyncData().conditionHolds ||
                                    (sd.agentID) != (myRow.getSyncData().agentID) || sd.transitionID != myRow.getSyncData().transitionID) {
                        // my sync row has changed
                        myRow.setSyncData(sd);
                        myRow.getReceivedBy().clear();
                        this.readyForSync = false;
                        myRow.getReceivedBy().add(this.myID);
                    } else {
                        if (CommonUtils.SP_DEBUG_debug) {
                            System.out.println("ChangeOwnData: SendTalk==false");
                        }
                        maySendTalk = false;
                    }
                } else // init my row
                {
                    SyncRow sr = new SyncRow(sd);
                    sr.getReceivedBy().add(this.myID);
                    this.myRow = sr;

                    this.syncMatrix.add(sr);
                }
            }
        }

        if (CommonUtils.SP_DEBUG_debug) {
            System.out.println();
            System.out.println("Matrix: ChangeOwnData");
//            printMatrix();
        }

        if (maySendTalk) {
            if (isSyncComplete()) {
                if (CommonUtils.SP_DEBUG_debug) {
                    System.out.println("ChangedOwnData: Synchronisation " + this.synchronisation.getID() + " ready");
                }
                sendSyncReady();
                this.readyForSync = true;
            } else {
                sendTalk(sd);
            }
        }
    }

    /**
     * resend lastTalk, test for failure on synchronisation timeout and stop sync on state change
     */
    boolean isValid(long curTick)
    {
        boolean stillActive = (this.lastTick + 2 >= curTick);

        if (!stillActive) {
            // notify others if i am part of the synchronisation already (i.e. have an own row)
            if (myRow != null) {
                if (myRow.hasData()) {
                    myRow.getSyncData().conditionHolds = false;

                    sendTalk(myRow.getSyncData());
                }
            }
            return false;
        }

        AlicaTime now = ae.getAlicaClock().now();

        if (this.lastTalkTime != AlicaTime.zero()) // talked already
        {
            System.out.println("TestTimeOut on Sync: " + this.synchronisation.getID());

            if ((now.time > this.synchronisation.getTalkTimeOut().time + this.lastTalkTime.time) && !this.readyForSync) {
            if (this.myRow != null) {
                sendTalk(this.myRow.getSyncData());
            }
        }
        }

        System.out.println("Synchronisation: TestTimeOut(): syncStarTime " + this.syncStartTime);

        if (this.synchronisation.isFailOnSyncTimeOut()) {
        if (now.time > this.synchronisation.getSyncTimeOut().time + this.syncStartTime.time) {
            System.out.println("Synchronisation: TestTimeOut() sync failed");
            return false;
        }
    }

        return true;
    }

    boolean integrateSyncTalk(SyncTalk talk, long curTick)
    {
        if (this.readyForSync) {
        // do not integrate talks if we believe the sync is already finished
        return true;
    }

        boolean isSynching = (this.lastTick + 2 >= curTick);

        if (!isSynching) {
            // do not accept messages (and send uneccessary ACKs) if we are not in a state for sync
            return false;
        }

        System.out.println("Integrate synctalk in synchronisation");
        System.out.println("ST: ElapsedTime: " + (ae.getAlicaClock().now().time - this.syncStartTime.time));

        for ( SyncData sd : talk.syncData) {
        System.out.println("syncdata for transID: " + sd.transitionID);

//        std::lock_guard<mutex> lock(syncMutex);
        synchronized (this)
            {
            SyncRow rowInMatrix = null;
            for (SyncRow row : this.syncMatrix) {
            System.out.println("ROW SD: " + row.getSyncData().agentID + " " + row.getSyncData().transitionID + " " + row.getSyncData().conditionHolds
                    + " " + row.getSyncData().ack);
            System.out.println("CUR SD: " + sd.agentID + " " + sd.transitionID + " " + sd.conditionHolds + " " + sd.ack);

            if (/*sd.ack == row.getSyncData().ack
                                                        &&*/
                    sd.conditionHolds == row.getSyncData().conditionHolds &&
                            (sd.agentID) == (row.getSyncData().agentID) && sd.transitionID == row.getSyncData().transitionID) {
                rowInMatrix = row;
                break;
            }
        }

            // if(rowInMatrix != null)
            if (rowInMatrix == null) {
                System.out.println("NEW MATRIX row");
                SyncRow newRow = new SyncRow(sd);
                newRow.getReceivedBy().add(talk.senderID);
                syncMatrix.add(newRow);
            } else {
                System.out.println("Received by: " + talk.senderID);
                rowInMatrix.getReceivedBy().add(talk.senderID);
            }
            if (isSyncComplete()) {
                System.out.println("IntegrateSyncTalk: Synctrans " + this.synchronisation.getID() + " ready");

                sendSyncReady();
                this.readyForSync = true;
            } else {
                // always reset this in case someone revokes his commitment
                this.readyForSync = false;
            }

            System.out.println("Matrix: IntSyncTalk");
            System.out.println(this);

            // late acks...
            if (this.readyForSync) {
            if (allSyncReady()) {
                System.out.println("SyncDONE in Synchronisation (IntTalk): elapsed time: " + (ae.getAlicaClock().now().time - syncStartTime.time));
                // notify syncmodul
                this.syncModul.synchronisationDone(this.synchronisation);
            }
        }
        }
    }

        return true;
    }

    void integrateSyncReady(SyncReady ready)
    {
        // every robot that has acknowleged my row needs to send me a SyncReady
        boolean found = false;
        for (SyncReady sr : this.receivedSyncReadys) {
        if ((sr.senderID) == (ready.senderID)) {
            found = true;
            break;
        }
    }

        if (!found) {
            this.receivedSyncReadys.add(ready);
        }
        System.out.println("Matrix: IntSyncReady");
        System.out.println(this);

        // check if all robots are ready
        if (this.readyForSync) {
        if (allSyncReady()) {
            // notify syncModul
            System.out.println("SyncDONE in Synchronisation (IntReady): elapsed time: " + (ae.getAlicaClock().now().time - this.syncStartTime.time));
            this.syncModul.synchronisationDone(this.synchronisation);
        }
    }
    }

    void setSynchronisation( Synchronisation synchronisation)
    {
        this.synchronisation = synchronisation;
    }

    boolean allSyncReady()
    {
        // test if all robots who acknowledged myRow have sent a SyncReady
        for (ID robotID : this.myRow.getReceivedBy()) {
        if (robotID != myID) // we do not necessarily need an ack from ourselves
        {
            boolean foundRobot = false;
            for ( SyncReady sr : this.receivedSyncReadys) {
            if (sr.senderID == robotID) {
                foundRobot = true;
                break;
            }
        }

            if (!foundRobot) // at least one robot is missing
            {
                return false;
            }
        }
    }
        return true;
    }

    void sendTalk( SyncData sd)
    {
        SyncTalk talk = new SyncTalk();
        talk.syncData.add(sd);
        this.lastTalkTime = ae.getAlicaClock().now();

        System.out.println("Sending Talk TID: " + sd.transitionID);

        this.syncModul.sendSyncTalk(talk);
    }

    void sendSyncReady()
    {
        // send own row again to be sure
        sendTalk(myRow.getSyncData());

        SyncReady sr = new SyncReady();
        sr.synchronisationID = this.synchronisation.getID();
        this.syncModul.sendSyncReady(sr);
    }

    /**
     * Before calling this method lock this mutex --> syncMutex (lock_guard<mutex> lock(syncMutex))
     */
    boolean isSyncComplete()
    {
        // myRow needs to be acknowledged by all participants
        // every participant needs to believe in its condition
        // there must be at least one participant for every condition
//        std::lock_guard<std::mutex> lock(rowOkMutex);
        synchronized (this) {
            this.rowsOK.clear();
            // collect participants
            for (long transID : this.connectedTransitions) {
                SyncRow foundRow = null;

                //			Is not needed here
                //			lock_guard<mutex> lock(syncMutex);
                for (SyncRow row : this.syncMatrix) {
                    if (row.getSyncData().transitionID == transID && row.getSyncData().conditionHolds) {
                        foundRow = row;
                        break;
                    }
                }
                if (foundRow == null) // no robot for transition
                {
                    return false;
                }

                if (!this.rowsOK.contains(foundRow)) {
                    this.rowsOK.add(foundRow);
                }
            }
            //		check for acks in own row
            if (this.myRow == null) {
                return false;
            }
            for (SyncRow row : this.rowsOK) {
                ID tmp = row.getSyncData().agentID;
                if (!this.myRow.getReceivedBy().contains(tmp)){
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        String s = "";
        s += "Matrix:" + "\n";

        for (SyncRow row : this.syncMatrix) {
        s += "Row: " + row.getSyncData().agentID + " "
                + row.getSyncData().transitionID + " " + row.getSyncData().conditionHolds + " " + row.getSyncData().ack
                + " RecvBy: ";
        for (ID robotID : row.getReceivedBy()) {
            s += robotID + ", ";
        }
        s += "\n";
    }
        s += "ReceivedSyncreadys: ";
        for ( SyncReady sr : this.receivedSyncReadys) {
        s += sr.senderID + ", " + "\n";
        ;
    }
        s += "\n";
        return s;
    }
}
