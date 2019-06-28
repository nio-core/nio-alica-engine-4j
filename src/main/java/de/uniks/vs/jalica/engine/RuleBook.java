package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.engine.common.SystemConfig;
import de.uniks.vs.jalica.engine.model.*;
import de.uniks.vs.jalica.engine.authority.AllocationDifference;
import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.planselection.PlanSelector;
import de.uniks.vs.jalica.engine.syncmodule.ISyncModule;
import de.uniks.vs.jalica.engine.syncmodule.SyncModule;
import de.uniks.vs.jalica.engine.teammanagement.TeamManager;
import de.uniks.vs.jalica.engine.teammanagement.TeamObserver;

import java.util.*;

/**
 * Created by alex on 13.07.17.
 * updated 22.6.19
 */
public class RuleBook {

    private final AlicaEngine ae;
    private TeamObserver to;
    private TeamManager tm;
    private SyncModule sm;
    private PlanSelector ps;
    private PlanBase pb;
    private Logger log;
    private int maxConsecutiveChanges;
    private boolean changeOccurred;

//    private final int maxConsecutiveChanges;
//    private Logger log;
//    private ITeamObserver teamObserver;
//    private IPlanSelector planSelector;
//    private AlicaEngine ae;
//    private boolean changeOccured;
//    private ISyncModule syncModul;

    public RuleBook(AlicaEngine ae, PlanBase pb) {
        this.ae = ae;
        this.to = ae.getTeamObserver();
        this.tm = ae.getTeamManager();
        this.ps = new PlanSelector(ae, pb);
        this.pb = pb;
        this.sm = ae.getSyncModul();
        this.log = ae.getLog();
        this.changeOccurred = true;
        this.maxConsecutiveChanges = Integer.valueOf((String) ae.getSystemConfig().get("Alica").get("Alica.MaxRuleApplications"));
        assert(this.to != null && this.tm != null && this.ps != null && this.pb != null && this.sm != null && this.log != null);
    }

    public PlanChange visit(RunningPlan r) {
        int changes = 0;
        boolean doDynAlloc = true;
        PlanChange changeRecord = PlanChange.NoChange;
        PlanChange msChange = PlanChange.NoChange;
        // obtain modification lock for visited plan
        ScopedWriteLock lck = r.getWriteLock(); // TODO: synchronize block or lock by condition

        do {
            msChange = updateChange(msChange, changeRecord);
            changeRecord = PlanChange.NoChange;
            changeRecord = updateChange(changeRecord, synchTransitionRule(r));
            PlanChange transChange = transitionRule(r);
            while (transChange != PlanChange.NoChange && ++changes < this.maxConsecutiveChanges) {
                changeRecord = updateChange(changeRecord, transChange);
                transChange = transitionRule(r);
            }
            changeRecord = updateChange(changeRecord, transitionRule(r));
            changeRecord = updateChange(changeRecord, topFailRule(r));
            changeRecord = updateChange(changeRecord, allocationRule(r));
            changeRecord = updateChange(changeRecord, authorityOverrideRule(r));

            if (doDynAlloc) {
                changeRecord = updateChange(changeRecord, dynamicAllocationRule(r));
                doDynAlloc = false;
            }
            changeRecord = updateChange(changeRecord, planAbortRule(r));
            if (changeRecord == PlanChange.FailChange) {
                return PlanChange.FailChange; // allow higher level to react
            }
            changeRecord = updateChange(changeRecord, planRedoRule(r));
            changeRecord = updateChange(changeRecord, planReplaceRule(r));
            // planReplace may retire the current plan.
            if (r.isRetired()) {
                return changeRecord;
            }

            PlanChange propChange = planPropagationRule(r);
            changeRecord = updateChange(changeRecord, propChange);

            if (propChange != PlanChange.NoChange) {
                break; // abort applying rules to this plan as propagation has occurred
            }

        } while (changeRecord != PlanChange.NoChange && ++changes < this.maxConsecutiveChanges);
        return msChange;
    }

    public PlanChange updateChange(PlanChange cur, PlanChange update) {

        if (update != PlanChange.NoChange) {
            this.changeOccurred = true;
        }
        if (cur == PlanChange.NoChange) {
            return update;
        }
        if (cur == PlanChange.FailChange) {
            return cur;
        }
        if (cur == PlanChange.InternalChange) {
            if (update != PlanChange.NoChange) {
                return update;
            }
        }
        if (update == PlanChange.FailChange) {
            return update;
        }
        return cur;
    }

    public RunningPlan initialisationRule( Plan masterPlan) {
        System.out.println("RB: Init-Rule called.");
        if (masterPlan.getEntryPoints().size() != 1) {
            System.out.println("RB: Masterplan does not have exactly one task!");
        }

        RunningPlan main = this.pb.makeRunningPlan(masterPlan);

        main.setAllocationNeeded(true);

        EntryPoint defep = masterPlan.getEntryPoints().get(0);
        main.getAssignment().setAllToInitialState(this.tm.getActiveAgentIds().get(), defep);
        main.activate();
        main.useEntryPoint(defep);
        this.log.eventOccurred("Init");
        return main;
    }

    PlanChange dynamicAllocationRule(RunningPlan r) {
        assert(!r.isRetired());
        System.out.println("RB: dynAlloc-Rule called.");
        System.out.println("RB: dynAlloc RP \n" + r);

        if (r.isAllocationNeeded() || r.isBehaviour()) {
            return PlanChange.NoChange;
        }
        if (r.getParent() == null) {
            return PlanChange.NoChange; // masterplan excluded
        }
        if (!r.getCycleManagement().mayDoUtilityCheck()) {
            return PlanChange.NoChange;
        }

        RunningPlan parent = r.getParent();

        ArrayList<Long> robots = new ArrayList<>();
        parent.getAssignment().getAgentsInState(parent.getActiveState(), robots);
        double curUtil = 0.0;
        RunningPlan newr = this.ps.getBestSimilarAssignment(r, robots, curUtil);

        if (newr == null) {
            return PlanChange.NoChange;
        }
        Plan p = (Plan)(r.getActivePlan());

        double possibleUtil = newr.getAssignment().getLastUtilityValue();
        System.out.println("RB: Old U " + curUtil + " | "
                + " New U:" + possibleUtil);
        CommonUtils.aboutWarningNotification(curUtil < -0.99, "#############Assignment is valid?: " + r.getAssignment().isValid() + "\n" + r);
        System.out.println("RB: New Assignment" + newr.getAssignment() + "\n" + "RB: Old Assignment" + r.getAssignment());

        if (possibleUtil - curUtil > p.getUtilityThreshold()) {
            // cout + "RB: AllocationDifference.Reason.utility " + endl;
            r.getCycleManagement().setNewAllocDiff(r.getAssignment(), newr.getAssignment(), AllocationDifference.Reason.utility);
         State before = r.getActiveState();
            r.adaptAssignment(newr);
            if (r.getActiveState() != null && r.getActiveState() != before) {
                r.setAllocationNeeded(true);
            }

            System.out.println("RB: B4 dynChange: Util is " + curUtil + " | "
                    + " suggested is " + possibleUtil + " | "
                    + " threshold " + p.getUtilityThreshold() + "\n"
                    + "RB: DynAlloc in " + p.getName());

            this.log.eventOccurred("DynAlloc(", p.getName(), ")");
            return PlanChange.InternalChange;
        }
        return PlanChange.NoChange;
    }

    PlanChange authorityOverrideRule(RunningPlan r) {
        assert(!r.isRetired());
        System.out.println("RB: AuthorityOverride-Rule called.");

        if (r.isBehaviour()) {
            return PlanChange.NoChange;
        }
        System.out.println("RB: AuthorityOverride RP \n" + r);

        if (r.getCycleManagement().isOverridden()) {
            if (r.getCycleManagement().applyAssignment()) {
               this.log.eventOccurred("AuthorityOverride(", r.getActivePlan().getName(), ")");
                System.out.println("RB: Authorative set assignment of " + r.getActivePlan().getName() + " is:" + r.getAssignment());
                return PlanChange.InternalChange;
            }
        }
        return PlanChange.NoChange;
    }

    PlanChange planAbortRule(RunningPlan r)
    {
        assert(!r.isRetired());
        if (r.isFailureHandlingNeeded())
            return PlanChange.NoChange;
        if (r.isBehaviour())
            return PlanChange.NoChange;
        if (r.getStatus() == PlanStatus.Status.Success)
            return PlanChange.NoChange;
        if (!r.getCycleManagement().mayDoUtilityCheck())
            return PlanChange.NoChange;

        if ((r.getActiveState() != null && r.getActiveState().isFailureState()) || !r.getAssignment().isValid() || !r.isRuntimeConditionValid()) {

        System.out.println("RB: PlanAbort-Rule called.");
        System.out.println("RB: PlanAbort RP \n" + r);
        System.out.println("RB: PlanAbort " + r.getActivePlan().getName());
        r.addFailure();
       this.log.eventOccurred("PAbort(", r.getActivePlan().getName(), ")");
        return PlanChange.FailChange;
    }
        return PlanChange.NoChange;
    }

    PlanChange planRedoRule(RunningPlan r)
    {
        assert(!r.isRetired());
        System.out.println("RB: PlanRedoRule-Rule called.");
        System.out.println("RB: PlanRedoRule RP \n" + r);

        if (r.getParent() == null || !r.isFailureHandlingNeeded() || r.isBehaviour())
            return PlanChange.NoChange;
        if (r.getFailureCount() != 1)
            return PlanChange.NoChange;
        if (r.getActiveEntryPoint() == null)
            return PlanChange.NoChange;
        if (r.getActiveState() == r.getActiveEntryPoint().getState()) {
        r.addFailure();
        System.out.println("RB: PlanRedoRule not executed for " + r.getActivePlan().getName()
                + "- Unable to repair, as the current state is already the initial state.");

        return PlanChange.FailChange;
    }
        r.setFailureHandlingNeeded(false);
        r.deactivateChildren();
        r.clearChildren();
        r.getAssignment().moveAllFromTo(r.getActiveEntryPoint(), r.getActiveState(), r.getActiveEntryPoint().getState());

        r.useState(r.getActiveEntryPoint().getState());
        r.setAllocationNeeded(true);

        System.out.println("RB: PlanRedoRule executed for " + r.getActivePlan().getName());

       this.log.eventOccurred("PRedo(", r.getActivePlan().getName(), ")");
        return PlanChange.InternalChange;
    }

    PlanChange planReplaceRule(RunningPlan r)
    {
        assert(!r.isRetired());
        System.out.println("RB: PlanReplace-Rule called.");
        System.out.println("RB: PlanReplace RP \n" + r);

        if (r.getParent() == null || !r.isFailureHandlingNeeded() || r.isBehaviour())
            return PlanChange.NoChange;
        if (r.getFailureCount() != 2)
            return PlanChange.NoChange;
        RunningPlan parent = r.getParent();
        {
//            ScopedWriteLock lck = parent.getWriteLock();
            synchronized (parent) {
                parent.deactivateChildren();
                parent.setFailedChild(r.getActivePlan());
                parent.setAllocationNeeded(true);
                parent.clearChildren();
            }
        }
        r.setFailureHandlingNeeded(false);

        System.out.println("RB: PlanReplace" + r.getActivePlan().getName());

       this.log.eventOccurred("PReplace(", r.getActivePlan().getName(), ")");
        return PlanChange.FailChange;
    }

    PlanChange planPropagationRule(RunningPlan r)
    {
        assert(!r.isRetired());
        System.out.println("RB: PlanPropagation-Rule called.");
        System.out.println("RB: PlanPropagation RP \n" + r);

        if (r.getParent() == null || !r.isFailureHandlingNeeded() || r.isBehaviour())
            return PlanChange.NoChange;
        if (r.getFailureCount() < 3)
            return PlanChange.NoChange;
        r.getParent().addFailure();
        r.setFailureHandlingNeeded(false);

        System.out.println("RB: PlanPropagation " + r.getActivePlan().getName());

       this.log.eventOccurred("PProp(", r.getActivePlan().getName(), ")");
        return PlanChange.FailChange;
    }

    PlanChange allocationRule(RunningPlan rp)
    {
        assert(!rp.isRetired());
        System.out.println("RB: Allocation-Rule called.");
        System.out.println("RB: Allocation RP \n" + rp);

        if (!rp.isAllocationNeeded()) {
            return PlanChange.NoChange;
        }
        rp.setAllocationNeeded(false);

        ArrayList<Long> agents = new ArrayList<>();
        rp.getAssignment().getAgentsInState(rp.getActiveState(), agents);

        System.out.println(rp.getActiveState().getPlans().size() + " Plans in State " + rp.getActiveState().getName());

        ArrayList<RunningPlan> children = new ArrayList<>();
        boolean ok =this.ps.getPlansForState(rp, rp.getActiveState().getPlans(), agents, children);
        if (!ok || children.size() < rp.getActiveState().getPlans().size()) {
        rp.addFailure();
        System.out.println("RB: PlanAllocFailed " + rp.getActivePlan().getName());
        return PlanChange.FailChange;
    }
        rp.addChildren(children);

        System.out.println("RB: after add children");
        System.out.println("RB: PlanAlloc " + rp.getActivePlan().getName());

        if (!children.isEmpty()) {
           this.log.eventOccurred("PAlloc(", rp.getActivePlan().getName(), " in State ", rp.getActiveState().getName(), ")");
            return PlanChange.InternalChange;
        }
        return PlanChange.NoChange;
    }

    PlanChange topFailRule(RunningPlan r)
    {
        assert(!r.isRetired());
        System.out.println("RB: TopFail-Rule called.");
        System.out.println("RB: TopFail RP \n" + r);

        if (r.getParent() != null)
            return PlanChange.NoChange;

        if (r.isFailureHandlingNeeded()) {
            r.setFailureHandlingNeeded(false);
            r.clearFailures();
            EntryPoint ep = ((Plan)r.getActivePlan()).getEntryPoints().get(0);

            r.useEntryPoint(ep);

            r.setAllocationNeeded(true);
            r.getAssignment().clear();
            r.getAssignment().setAllToInitialState(this.tm.getActiveAgentIds().get(), ep);
            r.useState(ep.getState());
            r.clearFailedChildren();

            System.out.println("RB: PlanTopFail " + r.getActivePlan().getName());

           this.log.eventOccurred("TopFail");
            return PlanChange.InternalChange;
        }
        return PlanChange.NoChange;
    }

    PlanChange transitionRule(RunningPlan r)
    {
        assert(!r.isRetired());
        System.out.println("RB: Transition-Rule called.");
        System.out.println("RB: Transition RP \n" + r);

        if (r.getActiveState() == null)
            return PlanChange.NoChange;
            State nextState = null;

        for (Transition t : r.getActiveState().getOutTransitions()) {
        if (t.getSynchronisation() != null)
            continue;
        if (t.evalCondition(r)) {
            nextState = t.getOutState();
            r.getConstraintStore().addCondition(t.getPreCondition());
            break;
        }
    }
        if (nextState == null) {
            return PlanChange.NoChange;
        }

        System.out.println("RB: Transition " + r.getActivePlan().getName());

        r.moveState(nextState);

        r.setAllocationNeeded(true);
       this.log.eventOccurred("Transition(", r.getActivePlan().getName(), " to State ", r.getActiveState().getName(), ")");
        if (r.getActiveState().isSuccessState())
        return PlanChange.SuccessChange;
    else if (r.getActiveState().isFailureState())
        return PlanChange.FailChange;
        return PlanChange.InternalChange;
    }

    PlanChange synchTransitionRule(RunningPlan r)
    {
        assert(!r.isRetired());
        System.out.println("RB: Sync-Rule called.");
        System.out.println("RB: Sync RP \n" + r);

        if (r.getActiveState() == null) {
            return PlanChange.NoChange;
        }

        State nextState = null;

        for (Transition t : r.getActiveState().getOutTransitions()) {
        if (t.getSynchronisation() == null) {
            continue;
        }
        if (this.sm.followTransition(t)) {
            if (t.evalCondition(r)) {
                nextState = t.getOutState();
                r.getConstraintStore().addCondition(t.getPreCondition());
                break;
            } else {
               this.sm.setSynchronisation(t, false);
            }
        } else {
           this.sm.setSynchronisation(t, t.evalCondition(r));
        }
    }
        if (nextState == null) {
            return PlanChange.NoChange;
        }

        System.out.println("RB: SynchTransition" + r.getActivePlan().getName());

        r.moveState(nextState);

        r.setAllocationNeeded(true);
       this.log.eventOccurred("SynchTrans(", r.getActivePlan().getName(), ")");
        if (r.getActiveState().isSuccessState())
        return PlanChange.SuccessChange;
    else if (r.getActiveState().isFailureState())
        return PlanChange.FailChange;
        return PlanChange.InternalChange;
    }
    // --- getter setter --------

    public boolean hasChangeOccurred() { return this.changeOccurred; }
    public void resetChangeOccurred() { this.changeOccurred = false; }
    public PlanSelector getPlanSelector()  { return this.ps; }




//    public RunningPlan initialisationRule(Plan masterPlan) {
//        if (CommonUtils.RULE_debug) System.out.println("RB: Init-Rule called." );
//
//        if (masterPlan.getEntryPoints().size() != 1) {
//            ae.abort("RB: Masterplan does not have exactly one task!");
//        }
//
//        RunningPlan master = new RunningPlan(ae, masterPlan);
//        master.setAssignment( new Assignment(masterPlan));
//
//        master.setAllocationNeeded(true);
//        master.setAgentsAvail(CommonUtils.move(teamObserver.getAvailableAgentIDs()));
//
//        EntryPoint firstEntryPoint = (EntryPoint) masterPlan.getEntryPoints().values().toArray()[0];
//
//        master.getAssignment().setAllToInitialState( CommonUtils.move(teamObserver.getAvailableAgentIDs()), firstEntryPoint);
//        master.activate();
//        master.setOwnEntryPoint(firstEntryPoint);
//        this.log.eventOccured("Init");
//        return master;
//
//    }
//
//    public boolean isChangeOccured() {
//        return changeOccured;
//    }
//
//    public void setChangeOccured(boolean changeOccured) {
//        this.changeOccured = changeOccured;
//    }
//
//    public PlanChange visit(RunningPlan runningPlan) {
//        int changes = 0;
//        boolean doDynAlloc = true;
//        PlanChange changeRecord = PlanChange.NoChange;
//        PlanChange msChange = PlanChange.NoChange;
//
//        do {
//            msChange = updateChange(msChange, changeRecord);
//
//            changeRecord = PlanChange.NoChange;
//            changeRecord = updateChange(changeRecord, synchTransitionRule(runningPlan));
//            PlanChange transChange = transitionRule(runningPlan);
//
//            while (transChange != PlanChange.NoChange && ++changes < this.maxConsecutiveChanges) {
//                changeRecord = updateChange(changeRecord, transChange);
//                transChange = transitionRule(runningPlan);
//            }
//            changeRecord = updateChange(changeRecord, transitionRule(runningPlan));
//            changeRecord = updateChange(changeRecord, topFailRule(runningPlan));
//            changeRecord = updateChange(changeRecord, allocationRule(runningPlan));
//            changeRecord = updateChange(changeRecord, authorityOverrideRule(runningPlan));
//
//            if (doDynAlloc) {
//                changeRecord = updateChange(changeRecord, dynamicAllocationRule(runningPlan));
//                doDynAlloc = false;
//            }
//            changeRecord = updateChange(changeRecord, planAbortRule(runningPlan));
//            changeRecord = updateChange(changeRecord, planRedoRule(runningPlan));
//            changeRecord = updateChange(changeRecord, planReplaceRule(runningPlan));
//            PlanChange propChange = planPropagationRule(runningPlan);
//            changeRecord = updateChange(changeRecord, propChange);
//
//            if (propChange != PlanChange.NoChange) {
//                break; //abort applying rules teamObserver this plan as propagation has occurred
//            }
//
//        } while (changeRecord != PlanChange.NoChange && ++changes < this.maxConsecutiveChanges);
//        return msChange;
//    }
//
//    private PlanChange planPropagationRule(RunningPlan r) {
//        if (CommonUtils.RULE_debug) System.out.println("RB: PlanPropagation-Rule called." );
//        if (CommonUtils.RULE_debug) System.out.println("RB: PlanPropagation RP \n" + r.toString() );
//        if (r.getParent() != null || !r.getFailHandlingNeeded() || r.isBehaviour())
//            return PlanChange.NoChange;
//        if (r.getFailure() < 3)
//            return PlanChange.NoChange;
//        r.getParent().addFailure();
//        r.setFailHandlingNeeded(false);
//
//        if (CommonUtils.RULE_debug) System.out.println("RB: PlanPropagation " + r.getPlan().getName() );
//        log.eventOccured("PProp(" + r.getPlan().getName() + ")");
//        return PlanChange.FailChange;
//    }
//
//
//    private PlanChange planReplaceRule(RunningPlan r) {
//
//        if (CommonUtils.RULE_debug) System.out.println( "RB: PlanReplace-Rule called." );
//        if (CommonUtils.RULE_debug) System.out.println( "RB: PlanReplace RP \n" + r.toString() );
////        if (r.getParent().expired()|| !r.getFailHandlingNeeded() || r.isBehaviour())
//        if (r.getParent()== null|| !r.getFailHandlingNeeded() || r.isBehaviour()) {
//            return PlanChange.NoChange;
//        }
//        if (r.getFailure() != 2) {
//            return PlanChange.NoChange;
//        }
////        auto temp = r.getParent().lock();
//        if (CommonUtils.RULE_debug) System.out.println( "RB: Plan has parent :"  + r.getParent() );
//        RunningPlan temp = r.getParent();
//        temp.deactivateChildren();
//        temp.setFailedChild(r.getPlan());
//        temp.setAllocationNeeded(true);
//        temp.clearChildren();
//        r.setFailHandlingNeeded(false);
//
//        if (CommonUtils.RULE_debug) System.out.println( "RB: PlanReplace" + r.getPlan().getName() );
//
//        log.eventOccured("PReplace(" + r.getPlan().getName() + ")");
//        return PlanChange.FailChange;
//    }
//
//    private PlanChange planRedoRule(RunningPlan r) {
//        if (CommonUtils.RULE_debug) System.out.println("RB: PlanRedoRule-Rule called." );
//        if (CommonUtils.RULE_debug) System.out.println("RB: PlanRedoRule RP \n" + r.toString() );
//
//        if (r.getParent() != null || !r.getFailHandlingNeeded() || r.isBehaviour())
//            return PlanChange.NoChange;
//        if (r.getFailure() != 1)
//            return PlanChange.NoChange;
//        if (r.getOwnEntryPoint() == null)
//            return PlanChange.NoChange;
//        if (r.getActiveState() == r.getOwnEntryPoint().getState())
//        {
////            r.addFailure();
//            if (CommonUtils.RULE_debug) System.out.println("RB: PlanRedoRule not executed for " + r.getPlan().getName() + "- Unable teamObserver repair, as the current state is already the initial state.");
//            return PlanChange.FailChange;
//        }
//        r.setFailHandlingNeeded(false);
//        r.deactivateChildren();
//        r.clearChildren();
//        Vector<Long> agents = new Vector<>(/*r.getAssignment().getAgentStateMapping().getAgentsInState(r.getActiveState()).size()*/);
//
//        CommonUtils.copy(r.getAssignment().getAgentStateMapping().getAgentsInState(r.getActiveState()),0,
//                r.getAssignment().getAgentStateMapping().getAgentsInState(r.getActiveState()).size()-1,
//                agents); // backinserter
//
//        r.getAssignment().getAgentStateMapping().setStates(agents, r.getOwnEntryPoint().getState());
//
//        r.setActiveState(r.getOwnEntryPoint().getState());
//        r.setAllocationNeeded(true);
//        if (CommonUtils.RULE_debug) System.out.println( "RB: PlanRedoRule executed for " + r.getPlan().getName() );
//        log.eventOccured("PRede(" + r.getPlan().getName() + ")");
//        return PlanChange.InternalChange;
//    }
//
//    private PlanChange planAbortRule(RunningPlan r) {
//        if (r.getFailHandlingNeeded())
//            return PlanChange.NoChange;
//        if (r.isBehaviour())
//            return PlanChange.NoChange;
//        if (r.getStatus() == PlanStatus.Success)
//            return PlanChange.NoChange;
//        if (!r.getCycleManagement().mayDoUtilityCheck())
//        return PlanChange.NoChange;
//
//        if ((r.getActiveState() != null && r.getActiveState().isFailureState()) || !r.getAssignment().isValid()
//                || !r.evalRuntimeCondition())
//        {
//            if (CommonUtils.RULE_debug) System.out.println("RB: PlanAbort-Rule called." );
//            if (CommonUtils.RULE_debug) System.out.println( "RB: PlanAbort RP \n" + r.toString() );
//            if (CommonUtils.RULE_debug) System.out.println("RB: PlanAbort " + r.getPlan().getName() );
//
//            r.addFailure();
//            log.eventOccured("PAbort(" + r.getPlan().getName() + ")");
//            return PlanChange.FailChange;
//        }
//        return PlanChange.NoChange;
//    }
//
//    private PlanChange dynamicAllocationRule(RunningPlan r) {
//        if (CommonUtils.RULE_debug) System.out.println("RB: dynAlloc-Rule called.");
//        if (CommonUtils.RULE_debug) System.out.println("RB: dynAlloc RP \n" + r.toString() );
//
//        if (r.isAllocationNeeded() || r.isBehaviour())
//        {
//            return PlanChange.NoChange;
//        }
////        if (r.getParent().expired())
//        if (r.getParent()== null)
//        {
//            return PlanChange.NoChange; //masterplan excluded
//        }
//        if (!r.getCycleManagement().mayDoUtilityCheck())
//        {
//            return PlanChange.NoChange;
//        }
//
//        RunningPlan temp = r.getParent();
//        Vector<Long> agents = new  Vector<>(/*temp.getAssignment().getAgentStateMapping().getAgentsInState(temp.getActiveState()).size()*/);
//        CommonUtils.copy(temp.getAssignment().getAgentStateMapping().getAgentsInState(temp.getActiveState()),0,
//                temp.getAssignment().getAgentStateMapping().getAgentsInState(temp.getActiveState()).size()-1,
//                agents);
//
//        RunningPlan newr = planSelector.getBestSimilarAssignment(r, new Vector<Long>(agents));
//
//        if (newr == null)
//        {
//            return PlanChange.NoChange;
//        }
//        double curUtil = 0;
//        if (!r.evalRuntimeCondition())
//        {
//            curUtil = -1.0;
//        }
//        else
//        {
//            curUtil = r.getPlan().getUtilityFunction().eval(r, r);
//        }
//
//        double possibleUtil = newr.getAssignment().getMax();
////#ifdef RULE_debug
//        if (CommonUtils.RULE_debug) System.out.println("RB: Old U " + curUtil + " | " + " New U:" + possibleUtil );
//        if(curUtil < -0.99) {
//            if (CommonUtils.RULE_debug) System.out.println( "#############Assignment is valid?: " + r.getAssignment().isValid() );
//            if (CommonUtils.RULE_debug) System.out.println( r.toString() );
//        }
//        if (CommonUtils.RULE_debug) System.out.println("RB: New Assignment" + newr.getAssignment().toString());
//        if (CommonUtils.RULE_debug) System.out.println( "RB: Old Assignment" + r.getAssignment().toString());
////remove comments
////#endif
//
//        if (possibleUtil - curUtil > r.getPlan().getUtilityThreshold())
//        {
//            //cout << "RB: AllocationDifference::Reason::utility " << endl;
//            r.getCycleManagement().setNewAllocDiff(r.getAssignment(), newr.getAssignment(), AllocationDifference.Reason.utility);
//            State before = r.getActiveState();
//            r.adaptAssignment(newr);
//            if (r.getActiveState() != null && r.getActiveState() != before)
//                r.setAllocationNeeded(true);
////#ifdef RULE_debug
//            if (CommonUtils.RULE_debug) System.out.println( "RB: B4 dynChange: Util is " + curUtil + " | " + " suggested is " + possibleUtil + " | " + " threshold " + r.getPlan().getUtilityThreshold() );
//            if (CommonUtils.RULE_debug) System.out.println( "RB: DynAlloc" +r.getPlan().getName());
////#endif
//
//            log.eventOccured("DynAlloc(" + r.getPlan().getName() + ")");
//            return PlanChange.InternalChange;
//        }
//        return PlanChange.NoChange;
//    }
//
//    private PlanChange authorityOverrideRule(RunningPlan r) {
//
////#ifdef RULE_debug
//        if (CommonUtils.RULE_debug) System.out.println( "RB: AuthorityOverride-Rule called." );
////#endif
//        if (r.isBehaviour())
//            return PlanChange.NoChange;
//
////#ifdef RULE_debug
//        if (CommonUtils.RULE_debug) System.out.println(  "RB: AuthorityOverride RP \n" + r.toString() );
////#endif
//        if (r.getCycleManagement().isOverridden())
//        {
//            if (r.getCycleManagement().setAssignment())
//            {
//                log.eventOccured("AuthorityOverride(" + r.getPlan().getName() + ")");
////#ifdef RULE_debug
//                if (CommonUtils.RULE_debug) System.out.println(  "RB: Authorative set assignment of " + r.getPlan().getName() + " is:" + r.getAssignment().toString());
////#endif
//                return PlanChange.InternalChange;
//            }
//        }
//        return PlanChange.NoChange;
//    }
//
//    private PlanChange allocationRule(RunningPlan rp) {
//
////#ifdef RULE_debug
//        if (CommonUtils.RULE_debug) System.out.println( "RB: Allocation-Rule called." );
//        if (CommonUtils.RULE_debug) System.out.println( "RB: Allocation RP \n" + rp.toString() );
////#endif
//        if (!rp.isAllocationNeeded())
//        {
//            return PlanChange.NoChange;
//        }
//        rp.setAllocationNeeded(false);
//
//        Vector<Long> agents = new Vector<Long>(/*rp.getAssignment().getAgentStateMapping().getAgentsInState(rp.getActiveState()).size()*/);
//
//        CommonUtils.copy(   rp.getAssignment().getAgentStateMapping().getAgentsInState(rp.getActiveState()),0,
//                rp.getAssignment().getAgentStateMapping().getAgentsInState(rp.getActiveState()).size()-1,
//                agents);
//
//        if (CommonUtils.RULE_debug) System.out.println( "RB: There are " + rp.getActiveState().getPlans().size() + " Plans in State " + rp.getActiveState().getName() );
//        ArrayList<RunningPlan> children = this.planSelector.getPlansForState( rp, rp.getActiveState().getPlans(), agents);
//
//        if (children == null || children.size() < rp.getActiveState().getPlans().size())
//        {
//            rp.addFailure();
//            if (CommonUtils.RULE_debug) System.out.println( "RB: PlanAllocFailed " + rp.getPlan().getName() );
//            return PlanChange.FailChange;
//        }
//        rp.addChildren(children);
//        if (CommonUtils.RULE_debug) System.out.println( "RB: after add children" );
//        if (CommonUtils.RULE_debug) System.out.println( "RB: PlanAlloc " +  rp.getPlan().getName() );
//
//        if (children.size() > 0)
//        {
//            log.eventOccured("PAlloc(" + rp.getPlan().getName() + " in State " + rp.getActiveState().getName() + ")");
//            return PlanChange.InternalChange;
//        }
//        return PlanChange.NoChange;
//    }
//
//    private PlanChange topFailRule(RunningPlan r) {
//        if (CommonUtils.RULE_debug) System.out.println( "RB: TopFail-Rule called." );
//        if (CommonUtils.RULE_debug) System.out.println( "RB: TopFail RP \n" + r.toString() );
////        if (!r.getParent().expired())
//        if (r.getParent() == null)
//
//            return PlanChange.NoChange;
//
//        if (r.getFailHandlingNeeded())
//        {
//            r.setFailHandlingNeeded(false);
//            r.clearFailures();
//
////            r.setOwnEntryPoint(((Plan)r.getPlan()).getEntryPoints().begin().second);
////            Set<Map.Entry<K,V>>
//            Set<HashMap.Entry<Long,EntryPoint>> set = ((Plan) r.getPlan()).getEntryPoints().entrySet();
//            HashMap.Entry<Long,EntryPoint>[] array = (HashMap.Entry<Long, EntryPoint>[]) set.toArray();
//            r.setOwnEntryPoint(array[0].getValue());
//
//            r.setAllocationNeeded(true);
//            r.setAgentsAvail(CommonUtils.move(teamObserver.getAvailableAgentIDs()));
//            r.getAssignment().clear();
//            r.getAssignment().setAllToInitialState(CommonUtils.move(teamObserver.getAvailableAgentIDs()), r.getOwnEntryPoint());
//            r.setActiveState(r.getOwnEntryPoint().getState());
//            r.clearFailedChildren();
////#ifdef RULE_debug
//            if (CommonUtils.RULE_debug) System.out.println( "RB: PlanTopFail" + r.getPlan().getName() );
////#endif
//            log.eventOccured("TopFail");
//            return PlanChange.InternalChange;
//        }
//        return PlanChange.NoChange;
//    }
//
//    private PlanChange synchTransitionRule(RunningPlan r) {
////        #ifdef RULE_debug
//        if (CommonUtils.RULE_debug) System.out.println( "RB: Sync-Rule called.");
//        if (CommonUtils.RULE_debug) System.out.println( "RB: Sync RP \n" + r.toString() );
////#endif
//        if (r.getActiveState() == null)
//        {
//            return PlanChange.NoChange;
//        }
//
//        State nextState = null;
//
//        for (Transition t : r.getActiveState().getOutTransitions())
//        {
//            if (t.getSyncTransition() == null)
//            {
//                continue;
//            }
//            if (this.syncModul.followSyncTransition(t))
//            {
//                if (t.evalCondition(r))
//                {
//                    nextState = t.getOutState();
//                    r.getConstraintStore().addCondition(((Condition)t.getPreCondition()));
//                    break;
//                }
//                else
//                {
//                    this.syncModul.setSynchronisation(t, false);
//                }
//            }
//			else
//            {
//                this.syncModul.setSynchronisation(t, t.evalCondition(r));
//            }
//        }
//        if (nextState == null)
//        {
//            return PlanChange.NoChange;
//        }
////#ifdef RULE_debug
//        if (CommonUtils.RULE_debug) System.out.println( "RB: SynchTransition" + r.getPlan().getName());
////#endif
//        r.moveState(nextState);
//
//        r.setAllocationNeeded(true);
//        log.eventOccured("SynchTrans(" + r.getPlan().getName() + ")");
//
//        if (r.getActiveState().isSuccessState())
//            return PlanChange.SuccessChange;
//
//        else if (r.getActiveState().isFailureState())
//            return PlanChange.FailChange;
//
//        return PlanChange.InternalChange;
//    }
//
//
//    private PlanChange transitionRule(RunningPlan runningPlan) {
////        #ifdef RULE_debug
//        if (CommonUtils.RULE_debug) System.out.println("RB: Transition-Rule called.");
//        if (CommonUtils.RULE_debug) System.out.println( "RB: Transition RP \n" + runningPlan.toString());
////#endif
//        if (runningPlan.getActiveState() == null)
//            return PlanChange.NoChange;
//        State nextState = null;
//
//        for (Transition transition : runningPlan.getActiveState().getOutTransitions()) {
//
//            if (transition.getSyncTransition() != null)
//                continue;
//
//            if (transition.evalCondition(runningPlan)) {
//                nextState = transition.getOutState();
//                runningPlan.getConstraintStore().addCondition(transition.getPreCondition());
//                break;
//            }
//        }
//        if (nextState == null) {
//            return PlanChange.NoChange;
//        }
//
//        if (CommonUtils.RULE_debug) System.out.println("RB: Transition " + runningPlan.getPlan().getName() );
//        runningPlan.moveState(nextState);
//
//        runningPlan.setAllocationNeeded(true);
//        log.eventOccured("Transition(" + runningPlan.getPlan().getName() + " teamObserver State " + runningPlan.getActiveState().getName() + ")");
//        if (runningPlan.getActiveState().isSuccessState())
//            return PlanChange.SuccessChange;
//		else if (runningPlan.getActiveState().isFailureState())
//            return PlanChange.FailChange;
//        return PlanChange.InternalChange;
//    }
//
//    PlanChange updateChange(PlanChange cur, PlanChange update) {
//        if (update != PlanChange.NoChange) {
//            this.changeOccured = true;
//        }
//        if (cur == PlanChange.NoChange) {
//            return update;
//        }
//        if (cur == PlanChange.FailChange) {
//            return cur;
//        }
//        if (cur == PlanChange.InternalChange) {
//            if (update != PlanChange.NoChange)
//            {
//                return update;
//            }
//        }
//        if (update == PlanChange.FailChange) {
//            return update;
//        }
//        return cur;
//    }
}
