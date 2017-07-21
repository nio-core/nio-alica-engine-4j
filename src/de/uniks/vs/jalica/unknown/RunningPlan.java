package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.behaviours.BasicBehaviour;
import de.uniks.vs.jalica.behaviours.BehaviourConfiguration;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.teamobserver.ITeamObserver;

import javax.tools.DocumentationTool;
import java.lang.reflect.Array;
import java.nio.channels.AsynchronousFileChannel;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by alex on 13.07.17.
 */
public class RunningPlan {
    private PlanType planType;
    private AbstractPlan plan;
    private BasicBehaviour basicBehaviour;
    private boolean active;
    private State activeState;
    private ArrayList<RunningPlan> children;
    private boolean behaviour;
    private EntryPoint ownEntryPoint;
    private RunningPlan parent;
    private boolean allocationNeeded;
    private ITeamObserver to;
    private IBehaviourPool bp;
    private ConditionStore constraintStore;
    private Assignment assignment;
    private AlicaEngine ae;
    private BehaviourConfiguration bc;
    private AlicaTime stateStartTime;
    private PlanStatus status;
    private EntryPoint activeEntryPoint;
    private int ownId;
    private HashMap<AbstractPlan, Integer> failedSubPlans;
    private boolean failHandlingNeeded;
    private int failCount;
    private ArrayList<Integer> robotsAvail;

    public RunningPlan(AlicaEngine ae, BehaviourConfiguration bc) {

        this.ae = ae;
        this.bc = bc;
    }

    public RunningPlan(AlicaEngine ae, PlanType pt) {
        this.plan = null;
        this.planType = pt;
        this.behaviour = false;
    }

    public AbstractPlan getPlan() {
        return plan;
    }

    public void setBasicBehaviour(BasicBehaviour basicBehaviour) {
        this.basicBehaviour = basicBehaviour;
    }


    void toMessage(ArrayList<Long> message, RunningPlan deepestNode, int depth, int curDepth) {

        if (this.isBehaviour()) {
            return;
        }

        if (this.activeState != null) {
            message.add(this.activeState.getId());
        } else {
            return;
        }

        if (curDepth > depth) {
            depth = curDepth;
//            deepestNode = shared_from_this();
        }

        if (this.children.size() > 0) {
            message.add(new Long(-1));
            for (RunningPlan r : this.children) {
                r.toMessage(message, deepestNode, depth, curDepth + 1);
            }
            message.add(new Long(-2));
        }
    }

    public void printRecursive() {

    }

    public PlanChange tick(RuleBook rules) {
        return null;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isBehaviour() {
        return behaviour;
    }

    public EntryPoint getOwnEntryPoint() {
        return ownEntryPoint;
    }

    public State getActiveState() {
        return activeState;
    }

    public RunningPlan getParent() {
        return parent;
    }

    public void setAllocationNeeded(boolean allocationNeeded) {
        this.allocationNeeded = allocationNeeded;
    }

    public void moveState(State nextState)
    {
        deactivateChildren();
        clearChildren();
        this.assignment.moveRobots(this.activeState, nextState);
        this.setActiveState(nextState);
        this.failedSubPlans.clear();
    }

    public void setActiveState(State s) {
        if (this.activeState != s)
        {
            this.activeState = s;
            this.stateStartTime.time = ae.getIAlicaClock().now().time;
            if (this.activeState != null)
            {
                if (this.activeState.isFailureState())
                {
                    this.status = PlanStatus.Failed;
                }
				else if (this.activeState.isSuccessState())
                {
                    this.assignment.getEpSuccessMapping().getRobots(this.activeEntryPoint).add(this.ownId);
                    this.to.getOwnEngineData().getSuccessMarks().markSuccessfull(this.plan,
                        this.activeEntryPoint);
                }
            }
        }
    }

    private void clearChildren() {
        this.children.clear();
    }

    private void deactivateChildren() {

        for (RunningPlan r : this.children) {
            r.deactivate();
        }
    }

    private void deactivate() {
        this.active = false;
        if (this.isBehaviour())
        {
            bp.stopBehaviour(this.plan);
//            bp.stopBehaviour(shared_from_this());
        }
		else
        {
            this.to.notifyRobotLeftPlan(this.plan);
        }
        revokeAllConstraints();
        deactivateChildren();
    }

    private void revokeAllConstraints() {
        this.constraintStore.clear();
    }

    public ConditionStore getConstraintStore() {
        return constraintStore;
    }

    public boolean getFailHandlingNeeded() {
        return failHandlingNeeded;
    }

    public void setFailHandlingNeeded(boolean failHandlingNeeded) {
        this.failHandlingNeeded = failHandlingNeeded;
    }

    public void clearFailures() {
        this.failCount = 0;
    }

    public void setOwnEntryPoint(EntryPoint value)
    {
        if (this.activeEntryPoint != value)
        {
            this.assignment.removeRobot(ownId);
            this.activeEntryPoint = value;
            if (this.activeEntryPoint != null)
            {
                this.setActiveState(this.activeEntryPoint.getState());
                this.assignment.addRobot(ownId, this.activeEntryPoint, this.activeState);
            }
        }
    }

    public void setRobotsAvail(ArrayList<Integer> robots)
    {
        this.robotsAvail.clear();
        this.robotsAvail = CommonUtils.move(robots);
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void clearFailedChildren() {
        this.failedSubPlans.clear();
    }

    public boolean isAllocationNeeded() {
        return allocationNeeded;
    }

    public void setPlan(BehaviourConfiguration plan) {
        this.plan = plan;
    }

    public void setParent(RunningPlan parent) {
        this.parent = parent;
    }

    public PlanType getPlanType() {
        return planType;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public boolean evalPreCondition() {
        if (this.plan == null)
        {
            System.err.println("Cannot Eval Condition, Plan is null" );
            try {
                throw new Exception();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (this.plan.getPreCondition() == null)
        {
            return true;
        }
        try
        {
            return this.plan.getPreCondition().evaluate(shared_from_this());
        }
        catch (Exception  e)
        {
            System.err.println("Exception in precondition: " + e.getMessage() );
            return false;
        }
    }

    public boolean evalRuntimeCondition() {
        if (this.plan == null)
        {
            System.err.println( "Cannot Eval Condition, Plan is null" );
            try {
                throw new Exception();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (this.plan.getRuntimeCondition() == null)
        {
            return true;
        }
        try
        {
            return this.plan.getRuntimeCondition().evaluate(shared_from_this());
        }
        catch (Exception e)
        {
            System.err.println("Exception in runtimecondition: " + this.plan.getName() + e.getMessage() );
            return false;
        }
    }

    public void addChildren(ArrayList<RunningPlan> runningPlans) {
        for (RunningPlan r : runningPlans)
        {
            r.setParent(shared_from_this());
            this.children.add(r);
            auto iter = this.failedSubPlans.find(r.getPlan());
            if (iter != this.failedSubPlans.end())
            {
                r.failCount = iter.second;
            }
            if (this.active)
            {
                r.activate();
            }
        }
    }
}
