package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.behaviours.BasicBehaviour;
import de.uniks.vs.jalica.behaviours.BehaviourConfiguration;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.teamobserver.ITeamObserver;

import javax.tools.DocumentationTool;
import java.lang.reflect.Array;
import java.nio.channels.AsynchronousFileChannel;
import java.util.*;

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
    private LinkedHashMap<AbstractPlan, Integer> failedSubPlans;
    private boolean failHandlingNeeded;
    private int failCount;
    private ArrayList<Integer> robotsAvail = new ArrayList<>();
    private CycleManager cycleManagement;
    private long id;

    public RunningPlan(AlicaEngine ae, BehaviourConfiguration bc) {

        this.ae = ae;
        this.bc = bc;
    }

    public RunningPlan(AlicaEngine ae, PlanType pt) {
        this.plan = null;
        this.planType = pt;
        this.behaviour = false;
    }

    public RunningPlan(AlicaEngine ae, Plan plan) {
        this.plan = plan;
        Vector<EntryPoint> epCol = new Vector<>();

//        transform(plan.getEntryPoints().begin(), plan.getEntryPoints().end(), back_inserter(epCol),
//                [](map<long, EntryPoint>::value_type& val)
//                            {	return val.second;
//                            }
//        );
        for (EntryPoint e:  plan.getEntryPoints().values()) {
            epCol.add(e);
        }
        Collections.sort(epCol);

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
        CommonUtils.aboutNoImpl();
    }

    public PlanChange tick(RuleBook rules) {
        CommonUtils.aboutNoImpl();
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

    public void clearChildren() {
        this.children.clear();
    }

    public void deactivateChildren() {

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

    public void setPlan(AbstractPlan plan) {
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
            return this.plan.getPreCondition().evaluate(this);
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
            return this.plan.getRuntimeCondition().evaluate(this);
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
            r.setParent(this);
            this.children.add(r);
            Integer iter = this.failedSubPlans.get(r.getPlan());

            // TODO: fix work around
            Integer last = 0;
            for (AbstractPlan key : failedSubPlans.keySet()) {
                last = failedSubPlans.get(key);
            }

            if (iter != last)
            {
                r.failCount = iter;
            }
            if (this.active)
            {
                r.activate();
            }
        }
    }

    protected void activate() {
        this.active = true;
        if (this.isBehaviour())
        {
            bp.startBehaviour(this);
        }
        this.attachPlanConstraints();
        for (RunningPlan r : this.children)
        {
            r.activate();
        }
    }

    private void attachPlanConstraints() {
        //		cout << "RP: attachPlanConstraints " << this.getPlan().getName() << endl;
        this.constraintStore.addCondition(this.plan.getPreCondition());
        this.constraintStore.addCondition(this.plan.getRuntimeCondition());
    }

    public CycleManager getCycleManagement() {
        return cycleManagement;
    }

    public ArrayList<RunningPlan> getChildren() {
        return children;
    }

    public void limitToRobots(Set<Integer> robots) {
        if (this.isBehaviour())
        {
            return;
        }
        if (!this.cycleManagement.mayDoUtilityCheck())
        {
            return;
        }
        boolean recurse = false;
        Vector<Integer> curRobots = this.assignment.getAllRobots();
        for (int r : curRobots)
        {
            if (CommonUtils.find(curRobots,0, curRobots.size()-1, r) == curRobots.lastElement())
            {
                if (this.activeState != null
                    && this.assignment.getRobotStateMapping().stateOfRobot(r) == this.activeState)
                {
                    recurse = true;
                }
                this.assignment.removeRobot(r);
            }
        }
        if (recurse)
        {
            for (RunningPlan c : this.children)
            {
                c.limitToRobots(robots);
            }
        }
    }

    public void adaptAssignment(RunningPlan r) {
        State newState = r.getAssignment().getRobotStateMapping().getState(this.ownId);
        r.getAssignment().getRobotStateMapping().reconsiderOldAssignment(this.assignment, r.getAssignment());
        boolean reactivate = false;

        if (this.activeState != newState)
        {
            this.active = false;
            this.deactivateChildren();
            this.revokeAllConstraints();
            this.clearChildren();
            this.addChildren(r.getChildren());
            reactivate = true;
        }
		else
        {
            Set<Integer> robotsJoined = r.getAssignment().getRobotStateMapping().getRobotsInState(newState);
            for (RunningPlan r1 : this.children)
            {
                r1.limitToRobots(robotsJoined);
            }
        }

        this.plan = r.getPlan();
        this.activeEntryPoint = r.getOwnEntryPoint();
        this.assignment = r.assignment;
        this.setActiveState(newState);
        if (reactivate)
        {
            this.activate();
        }

    }

    public void addFailure() {
        this.failCount++;
        this.failHandlingNeeded = true;
    }

    public PlanStatus getStatus() {
        if (this.basicBehaviour != null)
        {
            if (this.basicBehaviour.isSuccess())
            {
                //cout << "RP: " << this.plan.getName() << " BEH Success" << endl;
                return PlanStatus.Success;
            }
			else if (this.basicBehaviour.isFailure())
            {
                //cout << "RP: " << this.plan.getName() << " BEH Failed" << endl;
                return PlanStatus.Failed;
            }
			else
            {
                //cout << "RP: " << this.plan.getName() << " BEH Running" << endl;
                return PlanStatus.Running;
            }
        }
        if (this.assignment != null && this.assignment.isSuccessfull())
        {
            //cout << "RP: " << this.plan.getName() << " ASS Success" << endl;
            return PlanStatus.Success;
        }
        //cout << "RP: " << this.plan.getName() << " STATUS " << (this.status == PlanStatus::Running ? "RUNNING" : (this.status == PlanStatus::Success ? "SUCCESS" : "FAILED")) << endl;
        return this.status;
    }

    public int getFailure() {
        return this.failCount;
    }

    public void setFailedChild(AbstractPlan child) {

        // TODO: fix work around
        Integer last = 0;
        for (AbstractPlan key : failedSubPlans.keySet()) {
            last = failedSubPlans.get(key);
        }

        if (this.failedSubPlans.get(child) != last)
        {
            Integer intChild = this.failedSubPlans.get(child);
            this.failedSubPlans.put(child,intChild++);
        }
		else
        {
            this.failedSubPlans.put(child, 1);
        }
    }

    public long getId() {
        return id;
    }
}
