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
        SystemConfig sc = SystemConfig.getInstance();
        this.maxConsecutiveChanges = Integer.valueOf(sc.get("Alica").get("Alica.MaxRuleApplications"));
        this.changeOccured = true;
    }

    public RunningPlan initialisationRule(Plan masterPlan) {
        return null;
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

    private PlanChange allocationRule(RunningPlan rp) {
        
//#ifdef RULE_debug
        System.out.println( "RB: Allocation-Rule called." );
        System.out.println( "RB: Allocation RP \n" + rp.toString() );
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
        System.out.println( "RB: There are " + rp.getActiveState().getPlans().size() + " Plans in State " + rp.getActiveState().getName() );
//#endif
        ArrayList<RunningPlan> children = this.ps.getPlansForState(
                rp, rp.getActiveState().getPlans(),
                robots);
        if (children == null || children.size() < rp.getActiveState().getPlans().size())
        {
            rp.addFailure();
//#ifdef RULE_debug
            System.out.println( "RB: PlanAllocFailed " + rp.getPlan().getName() );
//#endif
            return PlanChange.FailChange;
        }
        rp.addChildren(children);
//#ifdef RULE_debug
        System.out.println( "RB: after add children" );
        System.out.println( "RB: PlanAlloc " +  rp.getPlan().getName() );
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
        System.out.println( "RB: TopFail-Rule called." );
        System.out.println( "RB: TopFail RP \n" + r.toString() );
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
            System.out.println( "RB: PlanTopFail" + r.getPlan().getName() );
//#endif
            log.eventOccured("TopFail");
            return PlanChange.InternalChange;
        }
        return PlanChange.NoChange;
    }

    private PlanChange synchTransitionRule(RunningPlan r) {
//        #ifdef RULE_debug
        System.out.println( "RB: Sync-Rule called.");
        System.out.println( "RB: Sync RP \n" + r.toString() );
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
        System.out.println( "RB: SynchTransition" + r.getPlan().getName());
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
        System.out.println("RB: Transition-Rule called.");
        System.out.println( "RB: Transition RP \n" + r.toString());
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
        System.out.println("RB: Transition " + r.getPlan().getName() );
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
