package de.uniks.vs.jalica.engine.syncmodule;

import de.uniks.vs.jalica.engine.IAlicaCommunication;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.PlanRepository;
import de.uniks.vs.jalica.engine.common.SyncData;
import de.uniks.vs.jalica.engine.model.Synchronisation;
import de.uniks.vs.jalica.engine.model.Transition;
import de.uniks.vs.jalica.engine.containers.SyncReady;
import de.uniks.vs.jalica.engine.containers.SyncTalk;
import de.uniks.vs.jalica.common.utils.CommonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * Created by alex on 13.07.17.
 * Updated 22.6.19
 */
public class SyncModule {

    private boolean running;
    private AlicaEngine ae;
    private long myId;
    private long ticks;
    private PlanRepository pr;
    private HashMap<Synchronisation, SynchronisationProcess> synchSet;
    private ArrayList<Synchronisation> synchronisations;
    private Lock lomutex;
    private IAlicaCommunication communicator;

    public SyncModule(AlicaEngine ae) {
        this.ae = ae;
        this.myId = -1;
        this.pr = null;
        this.running = false;
        this.ticks = 0;
        this.communicator = null;
        this.synchSet = new HashMap<Synchronisation, SynchronisationProcess>();
        this.synchronisations = new ArrayList<>();
    }

    public void init() {
//        lock_guard<mutex> lock(lomutex);
        synchronized (this) {
            this.ticks = 0;
            this.running = true;
            this.myId = ae.getTeamManager().getLocalAgentID();
            this.pr = this.ae.getPlanRepository();
            this.communicator = this.ae.getCommunicator();
        }
    }

    public void close() {
        this.running = false;
        System.out.println( "SynchModul: Closed SynchModul");
    }

    public void tick() {
        ArrayList<SynchronisationProcess> failedSyncs = new ArrayList<SynchronisationProcess>();
//        lock_guard<mutex> lock(lomutex);
        synchronized (this.synchSet) {
            for (Map.Entry<Synchronisation, SynchronisationProcess> iter : this.synchSet.entrySet()) {
                if (!iter.getValue().isValid(ticks)) {
                    failedSyncs.add(iter.getValue());
                }
                ticks++;
                for (SynchronisationProcess s : failedSyncs) {
                    this.synchSet.remove(s.getSynchronisation());
                }
            }
        }
    }

    public void setSynchronisation(Transition trans, boolean holds) {
        SynchronisationProcess s;
        SynchronisationProcess process = this.synchSet.get(trans.getSynchronisation());
        if (process != null) {
            process.setTick(this.ticks);
            process.changeOwnData(trans.getID(), holds);
        } else {
            s = new SynchronisationProcess(ae, myId, trans.getSynchronisation(), this);
            s.setTick(this.ticks);
            s.changeOwnData(trans.getID(), holds);
//        lock_guard<mutex> lock(this.lomutex);
            synchronized (this.synchSet) {
                synchSet.put(trans.getSynchronisation(), s);
            }
        }
    }

    void sendSyncTalk(SyncTalk st) {
        if (!this.ae.maySendMessages())
            return;
        st.senderID = this.myId;
        this.communicator.sendSyncTalk(st);
    }

    void sendSyncReady(SyncReady sr) {
        if (!this.ae.maySendMessages())
            return;
        sr.senderID = this.myId;
        communicator.sendSyncReady(sr);
    }

    void sendAcks(ArrayList<SyncData> syncDataList) {
        if (!this.ae.maySendMessages())
            return;
        SyncTalk st = new SyncTalk();
        st.senderID = this.myId;
        st.syncData = syncDataList;
        this.communicator.sendSyncTalk(st);
    }

    void synchronisationDone(Synchronisation sync) {
        if (CommonUtils.SM_DEBUG_debug) {
            System.out.print("SyncDONE in SYNCMODUL for synchronisationID: " + sync.getID() + "\n");
            System.out.print("Remove synchronisationProcess object for synchronisationID: " + sync.getID() + "\n");
        }
        this.synchSet.remove(sync);
        this.synchronisations.add(sync);

        if (CommonUtils.SM_DEBUG_debug)
            System.out.print("SM: SYNC TRIGGER TIME:" + this.ae.getAlicaClock().now().inMilliseconds() + "\n");
    }

    public boolean followTransition(Transition trans) {
        if (this.synchronisations.contains(trans.getSynchronisation())) {
            this.synchronisations.remove(trans.getSynchronisation());
            return true;
        }
        return false;
    }

    public void onSyncTalk(SyncTalk st) {
        if (!this.running || st.senderID == this.myId)
            return;
        if (this.ae.getTeamManager().isAgentIgnored(st.senderID))
            return;

        if (CommonUtils.SM_DEBUG_debug) System.out.print("SyncModul:Handle Synctalk" + "\n");

        ArrayList<SyncData> toAck = new ArrayList<>();
        for (SyncData sd : st.syncData) {
            if (CommonUtils.SM_DEBUG_debug) {
                System.out.print("SyncModul: TransID" + sd.transitionID + "\n");
                System.out.print("SyncModul: RobotID" + sd.agentID + "\n");
                System.out.print("SyncModul: Condition" + sd.conditionHolds + "\n");
                System.out.print("SyncModul: ACK" + sd.ack + "\n");
            }

            Transition trans = this.pr.getTransitions().get(sd.transitionID);
            Synchronisation synchronisation = null;

            if (trans != null) {
                if (trans.getSynchronisation() != null) {
                    synchronisation = trans.getSynchronisation();
                } else {
                    System.err.println("SyncModul: Transition " + trans.getID() + " is not connected to a Synchronisation" + "\n");
                    return;
                }
            } else {
                System.err.println("SyncModul: Could not find Element for Transition with ID: " + sd.transitionID + "\n");
                return;
            }

            SynchronisationProcess syncProc = null;
            boolean doAck = true;
            {
//            lock_guard<mutex> lock(lomutex);
                synchronized (this.synchSet) {
                    SynchronisationProcess i = this.synchSet.get(synchronisation);

                    if (i != null) {
                        syncProc = i;
                        syncProc.integrateSyncTalk(st, this.ticks);

                    } else {
                        syncProc = new SynchronisationProcess(ae, this.myId, synchronisation, this);
                        synchSet.put(synchronisation, syncProc);
                        doAck = syncProc.integrateSyncTalk(st, this.ticks);
                    }
                }
            }
            if (!sd.ack && (st.senderID) == (sd.agentID) && doAck) {
                toAck.add(sd);
            }
        }
        for (SyncData sd : toAck) {
            sd.ack = true;
        }
        if (toAck.size() > 0) {
            sendAcks(toAck);
        }
    }

    public void onSyncReady(SyncReady sr) {
        if (!this.running || (sr.senderID) == (this.myId))
            return;
        if (this.ae.getTeamManager().isAgentIgnored(sr.senderID))
            return;
        Synchronisation syncTrans = this.pr.getSynchronisations().get(sr.synchronisationID);

        if (syncTrans == null) {
            System.out.print("SyncModul: Unable to find synchronisation " + sr.synchronisationID + " send by " + sr.senderID + "\n");
            return;
        }
        {
//            lock_guard<mutex> lock(lomutex);
            synchronized (this.synchSet) {
                SynchronisationProcess i = this.synchSet.get(syncTrans);
                if (i != null) {
                    i.integrateSyncReady(sr);

                }
            }
        }
    }
}
