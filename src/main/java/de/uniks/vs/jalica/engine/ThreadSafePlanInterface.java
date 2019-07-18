package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.engine.model.AbstractPlan;
import de.uniks.vs.jalica.engine.model.EntryPoint;
import de.uniks.vs.jalica.engine.model.Plan;
import de.uniks.vs.jalica.engine.teammanagement.view.AssignmentView;

public class ThreadSafePlanInterface {

    private RunningPlan rp;

    public ThreadSafePlanInterface() {}

    public ThreadSafePlanInterface( RunningPlan rp) {
            this.rp = rp;
    }

    public boolean mapsTo( RunningPlan rp)  { return this.rp == rp; }

    // Non locked thread safe reads:
    // Reads of const members are thread safe (rp's lifetime is deemed long enough):

    public AlicaEngine getAlicaEngine()  { return this.rp.getAlicaEngine(); }

    // Obtain scoped lock:
    public ReadLockedPlanPointer getRunningPlan()  { return new ReadLockedPlanPointer(this.rp); }

    // Locked reads

    public SafeAssignmentView agentsInEntryPointOfHigherPlan( EntryPoint ep){

        if (ep == null) {
            return new SafeAssignmentView();
        }
        ReadLockedPlanPointer cur = new ReadLockedPlanPointer(rp);
        cur.moveUp();
        while (cur.get() != null) {
            assert(cur.get().getActivePlan() != null);

            if (cur.get().getActivePlan() == ep.getPlan()) {
                return new SafeAssignmentView(cur.get().getAssignment(), ep.getIndex(), cur);
            }
            cur.moveUp();
        }
        return new SafeAssignmentView();
    }

    public SafeAssignmentView agentsInEntryPoint( EntryPoint ep) {

        if (ep == null) {
            return new SafeAssignmentView();
        }
        ReadLockedPlanPointer cur = new ReadLockedPlanPointer(this.rp);
        cur.moveUp();
        if (cur != null) {
            return new SafeAssignmentView(cur.get().getAssignment(), ep.getIndex(), cur);
        }
        return new SafeAssignmentView();
    }

    public EntryPoint getParentEntryPoint( String taskName) {
         Plan parentPlan = null;
        {
            ReadLockedPlanPointer cur = new ReadLockedPlanPointer(this.rp);
            cur.moveUp();

            if (cur == null) {
                return null;
            }
            parentPlan = (Plan)(cur.get().getActivePlan());
        }
        for ( EntryPoint e : parentPlan.getEntryPoints()) {

            if (e.getTask().getName() == taskName) {
                return e;
            }
        }
        return null;
    }

    public EntryPoint getHigherEntryPoint( String planName,  String taskName) {
        Plan parentPlan = null;
        {
            ReadLockedPlanPointer cur = new ReadLockedPlanPointer(this.rp);
            cur.moveUp();
            while (cur != null) {

                if (cur.get().getActivePlan().getName() == planName) {
                    parentPlan = (Plan)(cur.get().getActivePlan());
                    break;
                }
                cur.moveUp();
            }
        }
        for ( EntryPoint e : parentPlan.getEntryPoints()) {
            if (e.getTask().getName() == taskName) {
                return e;
            }
        }
        return null;
    }

    public AbstractPlan getActivePlan() {
        ScopedReadLock lck = new ScopedReadLock(this.rp.getReadLock());
        return this.rp.getActivePlan();
    }




    public class SafeAssignmentView extends AssignmentView {

        private ScopedReadLock lck;

        public SafeAssignmentView() {
            super();
            lck = new ScopedReadLock();
        }

        public SafeAssignmentView( Assignment a, int epIdx, ReadLockedPlanPointer rlpp) {
            super(a, epIdx);
            this.lck = new ScopedReadLock(rlpp.moveLock());
        }
    }

    private class ReadLockedPlanPointer {

        private RunningPlan rp;
        private ScopedReadLock lck;

        public ReadLockedPlanPointer( RunningPlan rp) {
            this.rp = rp;
            this.lck = (rp.getReadLock());
        }

        public void moveUp() {

            if (this.rp != null) {
                this.rp = this.rp.getParent();
                // make sure the lock is released before acquiring the parent's to avoid a potential deadlock
                this.lck.unlock();

                if (this.rp != null) {
                    this.lck = this.rp.getReadLock();
                }
            }
        }

        public ScopedReadLock  moveLock() {
            ScopedReadLock tmp = this.lck;
            this.lck = null;
            return tmp;
        }

        public boolean exist() { return this.rp != null; }
        public ScopedReadLock getLock() { return this.lck; }
        public RunningPlan get() { return this.rp; }
    }
}
