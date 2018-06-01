package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.common.Logger;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.supplementary.SystemConfig;
import de.uniks.vs.jalica.teamobserver.ITeamObserver;
import de.uniks.vs.jalica.teamobserver.TeamObserver;

import java.util.*;

/**
 * Created by alex on 13.07.17.
 */
public class RuleBook {

    private final int maxConsecutiveChanges;
    private Logger log;
    private ITeamObserver to;
    private IPlanSelector ps;
    private AlicaEngine ae;
    private boolean changeOccured;
    private ISyncModul sm;

    public RuleBook(AlicaEngine ae) {
        this.ae = ae;
        this.to = ae.getTeamObserver();
        this.ps = ae.getPlanSelector();
        this.sm = ae.getSyncModul();
        this.log = ae.getLog();
        SystemConfig sc = ae.getSystemConfig();
        this.maxConsecutiveChanges = Integer.valueOf((String) sc.get("Alica").get("Alica.MaxRuleApplications"));
        this.changeOccured = true;
    }

    public RunningPlan initialisationRule(Plan masterPlan) {
//        #ifdef RULE_debug
        if (CommonUtils.RULE_debug) System.out.println("RB: Init-Rule called." );
//#endif
        if (masterPlan.getEntryPoints().size() != 1)
        {
            ae.abort("RB: Masterplan does not have exactly one task!");
        }

        RunningPlan main = new RunningPlan(ae, masterPlan);
        main.setAssignment( new Assignment(masterPlan));

        main.setAllocationNeeded(true);
        main.setRobotsAvail(CommonUtils.move(to.getAvailableRobotIds()));

        EntryPoint defep = null;
        ArrayList<EntryPoint> l;
        defep = (EntryPoint) masterPlan.getEntryPoints().values().toArray()[0];//.begin().second;

        main.getAssignment().setAllToInitialState( CommonUtils.move(to.getAvailableRobotIds()), defep);
        main.activate();
        main.setOwnEntryPoint(defep);
        this.log.eventOccured("Init");
        return main;

    }

    public boolean isChangeOccured() {
        return changeOccured;
    }

    public void setChangeOccured(boolean changeOccured) {
        this.changeOccured = changeOccured;
    }

    public PlanChange visit(RunningPlan r) {
        int changes = 0;
        boolean doDynAlloc = true;
        PlanChange changeRecord = PlanChange.NoChange;
        PlanChange msChange = PlanChange.NoChange;

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
            changeRecord = updateChange(changeRecord, planRedoRule(r));
            changeRecord = updateChange(changeRecord, planReplaceRule(r));

            PlanChange propChange = planPropagationRule(r);
            changeRecord = updateChange(changeRecord, propChange);


            if (propChange != PlanChange.NoChange) {
                break; //abort applying rules to this plan as propagation has occurred
            }

        } while (changeRecord != PlanChange.NoChange && ++changes < this.maxConsecutiveChanges);
        return msChange;
    }

    private PlanChange planPropagationRule(RunningPlan r) {
//        #ifdef RULE_debug
        if (CommonUtils.RULE_debug) System.out.println("RB: PlanPropagation-Rule called." );
        if (CommonUtils.RULE_debug) System.out.println("RB: PlanPropagation RP \n" + r.toString() );
//#endif
        if (r.getParent() != null || !r.getFailHandlingNeeded() || r.isBehaviour())
            return PlanChange.NoChange;
        if (r.getFailure() < 3)
            return PlanChange.NoChange;
        r.getParent().addFailure();
        r.setFailHandlingNeeded(false);

//#ifdef RULE_debug
        if (CommonUtils.RULE_debug) System.out.println("RB: PlanPropagation " + r.getPlan().getName() );
//#endif
        log.eventOccured("PProp(" + r.getPlan().getName() + ")");
        return PlanChange.FailChange;
    }



    private PlanChange planReplaceRule(RunningPlan r) {
        
//#ifdef RULE_debug
        if (CommonUtils.RULE_debug) System.out.println( "RB: PlanReplace-Rule called." );
        if (CommonUtils.RULE_debug) System.out.println( "RB: PlanReplace RP \n" + r.toString() );
//#endif
//        if (r.getParent().expired()|| !r.getFailHandlingNeeded() || r.isBehaviour())
        if (r.getParent()!= null|| !r.getFailHandlingNeeded() || r.isBehaviour())
            return PlanChange.NoChange;
        if (r.getFailure() != 2)
            return PlanChange.NoChange;
//        auto temp = r.getParent().lock();
        RunningPlan temp = r.getParent();
        temp.deactivateChildren();
        temp.setFailedChild(r.getPlan());
        temp.setAllocationNeeded(true);
        temp.clearChildren();
        r.setFailHandlingNeeded(false);

//#ifdef RULE_debug
        if (CommonUtils.RULE_debug) System.out.println( "RB: PlanReplace" + r.getPlan().getName() );
//#endif
        log.eventOccured("PReplace(" + r.getPlan().getName() + ")");
        return PlanChange.FailChange;
    }

    private PlanChange planRedoRule(RunningPlan r) {
//        #ifdef RULE_debug
        if (CommonUtils.RULE_debug) System.out.println("RB: PlanRedoRule-Rule called." );
        if (CommonUtils.RULE_debug) System.out.println("RB: PlanRedoRule RP \n" + r.toString() );
//#endif
        if (r.getParent() != null || !r.getFailHandlingNeeded() || r.isBehaviour())
            return PlanChange.NoChange;
        if (r.getFailure() != 1)
            return PlanChange.NoChange;
        if (r.getOwnEntryPoint() == null)
            return PlanChange.NoChange;
        if (r.getActiveState() == r.getOwnEntryPoint().getState())
        {
//            r.addFailure();
//#ifdef RULE_debug
            if (CommonUtils.RULE_debug) System.out.println("RB: PlanRedoRule not executed for " + r.getPlan().getName() + "- Unable to repair, as the current state is already the initial state.");
//#endif
            return PlanChange.FailChange;
        }
        r.setFailHandlingNeeded(false);
        r.deactivateChildren();
        r.clearChildren();
        Vector<Integer> robots = new Vector<>(r.getAssignment().getRobotStateMapping().getRobotsInState(r.getActiveState()).size());

        CommonUtils.copy(r.getAssignment().getRobotStateMapping().getRobotsInState(r.getActiveState()),0,
                r.getAssignment().getRobotStateMapping().getRobotsInState(r.getActiveState()).size()-1,
                robots); // backinserter

        r.getAssignment().getRobotStateMapping().setStates(robots, r.getOwnEntryPoint().getState());

        r.setActiveState(r.getOwnEntryPoint().getState());
        r.setAllocationNeeded(true);
//#ifdef RULE_debug
        if (CommonUtils.RULE_debug) System.out.println( "RB: PlanRedoRule executed for " + r.getPlan().getName() );
//#endif
        log.eventOccured("PRede(" + r.getPlan().getName() + ")");
        return PlanChange.InternalChange;
    }

    private PlanChange planAbortRule(RunningPlan r) {
        if (r.getFailHandlingNeeded())
            return PlanChange.NoChange;
        if (r.isBehaviour())
            return PlanChange.NoChange;
        if (r.getStatus() == PlanStatus.Success)
            return PlanChange.NoChange;
        if (!r.getCycleManagement().mayDoUtilityCheck())
        return PlanChange.NoChange;

        if ((r.getActiveState() != null && r.getActiveState().isFailureState()) || !r.getAssignment().isValid()
                || !r.evalRuntimeCondition())
        {
//#ifdef RULE_debug
            if (CommonUtils.RULE_debug) System.out.println("RB: PlanAbort-Rule called." );
            if (CommonUtils.RULE_debug) System.out.println( "RB: PlanAbort RP \n" + r.toString() );
            if (CommonUtils.RULE_debug) System.out.println("RB: PlanAbort " + r.getPlan().getName() );
//#endif
            r.addFailure();
            log.eventOccured("PAbort(" + r.getPlan().getName() + ")");
            return PlanChange.FailChange;
        }
        return PlanChange.NoChange;
    }

    private PlanChange dynamicAllocationRule(RunningPlan r) {
//        #ifdef RULE_debug
        if (CommonUtils.RULE_debug) System.out.println("RB: dynAlloc-Rule called.");
        if (CommonUtils.RULE_debug) System.out.println("RB: dynAlloc RP \n" + r.toString() );
//#endif
        if (r.isAllocationNeeded() || r.isBehaviour())
        {
            return PlanChange.NoChange;
        }
//        if (r.getParent().expired())
        if (r.getParent()== null)
        {
            return PlanChange.NoChange; //masterplan excluded
        }
        if (!r.getCycleManagement().mayDoUtilityCheck())
        {
            return PlanChange.NoChange;
        }

//        temp = r.getParent().lock();
        RunningPlan temp = r.getParent();
        Vector<Integer> robots = new  Vector<Integer>(temp.getAssignment().getRobotStateMapping().getRobotsInState(temp.getActiveState()).size());
        CommonUtils.copy(temp.getAssignment().getRobotStateMapping().getRobotsInState(temp.getActiveState()),0,
                temp.getAssignment().getRobotStateMapping().getRobotsInState(temp.getActiveState()).size()-1,
                robots);
        RunningPlan newr = ps.getBestSimilarAssignment(r, new Vector<Integer>(robots));
        if (newr == null)
        {
            return PlanChange.NoChange;
        }
        double curUtil = 0;
        if (!r.evalRuntimeCondition())
        {
            curUtil = -1.0;
        }
        else
        {
            curUtil = r.getPlan().getUtilityFunction().eval(r, r);
        }
        double possibleUtil = newr.getAssignment().getMax();
//#ifdef RULE_debug
        if (CommonUtils.RULE_debug) System.out.println("RB: Old U " + curUtil + " | " + " New U:" + possibleUtil );
        if(curUtil < -0.99) {
            if (CommonUtils.RULE_debug) System.out.println( "#############Assignment is valid?: " + r.getAssignment().isValid() );
            if (CommonUtils.RULE_debug) System.out.println( r.toString() );
        }
        if (CommonUtils.RULE_debug) System.out.println("RB: New Assignment" + newr.getAssignment().toString());
        if (CommonUtils.RULE_debug) System.out.println( "RB: Old Assignment" + r.getAssignment().toString());
//remove comments
//#endif

        if (possibleUtil - curUtil > r.getPlan().getUtilityThreshold())
        {
            //cout << "RB: AllocationDifference::Reason::utility " << endl;
            r.getCycleManagement().setNewAllocDiff(r.getAssignment(), newr.getAssignment(), AllocationDifference.Reason.utility);
            State before = r.getActiveState();
            r.adaptAssignment(newr);
            if (r.getActiveState() != null && r.getActiveState() != before)
                r.setAllocationNeeded(true);
//#ifdef RULE_debug
            if (CommonUtils.RULE_debug) System.out.println( "RB: B4 dynChange: Util is " + curUtil + " | " + " suggested is " + possibleUtil + " | " + " threshold " + r.getPlan().getUtilityThreshold() );
            if (CommonUtils.RULE_debug) System.out.println( "RB: DynAlloc" +r.getPlan().getName());
//#endif

            log.eventOccured("DynAlloc(" + r.getPlan().getName() + ")");
            return PlanChange.InternalChange;
        }
        return PlanChange.NoChange;

    }

    private PlanChange authorityOverrideRule(RunningPlan r) {

//#ifdef RULE_debug
        if (CommonUtils.RULE_debug) System.out.println( "RB: AuthorityOverride-Rule called." );
//#endif
        if (r.isBehaviour())
            return PlanChange.NoChange;

//#ifdef RULE_debug
        if (CommonUtils.RULE_debug) System.out.println(  "RB: AuthorityOverride RP \n" + r.toString() );
//#endif
        if (r.getCycleManagement().isOverridden())
        {
            if (r.getCycleManagement().setAssignment())
            {
                log.eventOccured("AuthorityOverride(" + r.getPlan().getName() + ")");
//#ifdef RULE_debug
                if (CommonUtils.RULE_debug) System.out.println(  "RB: Authorative set assignment of " + r.getPlan().getName() + " is:" + r.getAssignment().toString());
//#endif
                return PlanChange.InternalChange;
            }
        }
        return PlanChange.NoChange;
    }

    private PlanChange allocationRule(RunningPlan rp) {
        
//#ifdef RULE_debug
        if (CommonUtils.RULE_debug) System.out.println( "RB: Allocation-Rule called." );
        if (CommonUtils.RULE_debug) System.out.println( "RB: Allocation RP \n" + rp.toString() );
//#endif
        if (!rp.isAllocationNeeded())
        {
            return PlanChange.NoChange;
        }
        rp.setAllocationNeeded(false);

        Vector<Integer> robots = new Vector<Integer>(rp.getAssignment().getRobotStateMapping().getRobotsInState(rp.getActiveState()).size());

        CommonUtils.copy(   rp.getAssignment().getRobotStateMapping().getRobotsInState(rp.getActiveState()),0,
                rp.getAssignment().getRobotStateMapping().getRobotsInState(rp.getActiveState()).size()-1,
                robots);

//#ifdef RULE_debug
        if (CommonUtils.RULE_debug) System.out.println( "RB: There are " + rp.getActiveState().getPlans().size() + " Plans in State " + rp.getActiveState().getName() );
//#endif
        ArrayList<RunningPlan> children = this.ps.getPlansForState(
                rp, rp.getActiveState().getPlans(),
                robots);
        if (children == null || children.size() < rp.getActiveState().getPlans().size())
        {
            rp.addFailure();
//#ifdef RULE_debug
            if (CommonUtils.RULE_debug) System.out.println( "RB: PlanAllocFailed " + rp.getPlan().getName() );
//#endif
            return PlanChange.FailChange;
        }
        rp.addChildren(children);
//#ifdef RULE_debug
        if (CommonUtils.RULE_debug) System.out.println( "RB: after add children" );
        if (CommonUtils.RULE_debug) System.out.println( "RB: PlanAlloc " +  rp.getPlan().getName() );
//#endif

        if (children.size() > 0)
        {
            log.eventOccured("PAlloc(" + rp.getPlan().getName() + " in State " + rp.getActiveState().getName() + ")");
            return PlanChange.InternalChange;
        }
        return PlanChange.NoChange;
    }

    private PlanChange topFailRule(RunningPlan r) {
//        #ifdef RULE_debug
        if (CommonUtils.RULE_debug) System.out.println( "RB: TopFail-Rule called." );
        if (CommonUtils.RULE_debug) System.out.println( "RB: TopFail RP \n" + r.toString() );
//#endif
//        if (!r.getParent().expired())
        if (r.getParent() == null)

            return PlanChange.NoChange;

        if (r.getFailHandlingNeeded())
        {
            r.setFailHandlingNeeded(false);
            r.clearFailures();

//            r.setOwnEntryPoint(((Plan)r.getPlan()).getEntryPoints().begin().second);
//            Set<Map.Entry<K,V>>
            Set<HashMap.Entry<Long,EntryPoint>> set = ((Plan) r.getPlan()).getEntryPoints().entrySet();
            HashMap.Entry<Long,EntryPoint>[] array = (HashMap.Entry<Long, EntryPoint>[]) set.toArray();
            r.setOwnEntryPoint(array[0].getValue());

            r.setAllocationNeeded(true);
            r.setRobotsAvail(CommonUtils.move(to.getAvailableRobotIds()));
            r.getAssignment().clear();
            r.getAssignment().setAllToInitialState(CommonUtils.move(to.getAvailableRobotIds()), r.getOwnEntryPoint());
            r.setActiveState(r.getOwnEntryPoint().getState());
            r.clearFailedChildren();
//#ifdef RULE_debug
            if (CommonUtils.RULE_debug) System.out.println( "RB: PlanTopFail" + r.getPlan().getName() );
//#endif
            log.eventOccured("TopFail");
            return PlanChange.InternalChange;
        }
        return PlanChange.NoChange;
    }

    private PlanChange synchTransitionRule(RunningPlan r) {
//        #ifdef RULE_debug
        if (CommonUtils.RULE_debug) System.out.println( "RB: Sync-Rule called.");
        if (CommonUtils.RULE_debug) System.out.println( "RB: Sync RP \n" + r.toString() );
//#endif
        if (r.getActiveState() == null)
        {
            return PlanChange.NoChange;
        }

        State nextState = null;

        for (Transition t : r.getActiveState().getOutTransitions())
        {
            if (t.getSyncTransition() == null)
            {
                continue;
            }
            if (this.sm.followSyncTransition(t))
            {
                if (t.evalCondition(r))
                {
                    nextState = t.getOutState();
                    r.getConstraintStore().addCondition(((Condition)t.getPreCondition()));
                    break;
                }
                else
                {
                    this.sm.setSynchronisation(t, false);
                }
            }
			else
            {
                this.sm.setSynchronisation(t, t.evalCondition(r));
            }
        }
        if (nextState == null)
        {
            return PlanChange.NoChange;
        }
//#ifdef RULE_debug
        if (CommonUtils.RULE_debug) System.out.println( "RB: SynchTransition" + r.getPlan().getName());
//#endif
        r.moveState(nextState);

        r.setAllocationNeeded(true);
        log.eventOccured("SynchTrans(" + r.getPlan().getName() + ")");

        if (r.getActiveState().isSuccessState())
            return PlanChange.SuccesChange;

        else if (r.getActiveState().isFailureState())
            return PlanChange.FailChange;

        return PlanChange.InternalChange;
    }


    private PlanChange transitionRule(RunningPlan r) {
//        #ifdef RULE_debug
        if (CommonUtils.RULE_debug) System.out.println("RB: Transition-Rule called.");
        if (CommonUtils.RULE_debug) System.out.println( "RB: Transition RP \n" + r.toString());
//#endif
        if (r.getActiveState() == null)
            return PlanChange.NoChange;
        State nextState = null;

        for (Transition t : r.getActiveState().getOutTransitions())
        {
            if (t.getSyncTransition() != null)
                continue;
            if (t.evalCondition(r))
            {
                nextState = t.getOutState();
                r.getConstraintStore().addCondition((Condition)t.getPreCondition());
                break;
            }
        }
        if (nextState == null)
        {
            return PlanChange.NoChange;
        }
//#ifdef RULE_debug
        if (CommonUtils.RULE_debug) System.out.println("RB: Transition " + r.getPlan().getName() );
//#endif
        r.moveState(nextState);

        r.setAllocationNeeded(true);
        log.eventOccured("Transition(" + r.getPlan().getName() + " to State " + r.getActiveState().getName() + ")");
        if (r.getActiveState().isSuccessState())
        return PlanChange.SuccesChange;
		else if (r.getActiveState().isFailureState())
        return PlanChange.FailChange;
        return PlanChange.InternalChange;
    }

    PlanChange updateChange(PlanChange cur, PlanChange update) {
        if (update != PlanChange.NoChange) {
            this.changeOccured = true;
        }
        if (cur == PlanChange.NoChange) {
            return update;
        }
        if (cur == PlanChange.FailChange) {
            return cur;
        }
        if (cur == PlanChange.InternalChange) {
            if (update != PlanChange.NoChange)
            {
                return update;
            }
        }
        if (update == PlanChange.FailChange) {
            return update;
        }
        return cur;
    }
}
