package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.common.ExtArrayList;
import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.common.SystemConfig;
import de.uniks.vs.jalica.engine.idmanagement.ID;
import de.uniks.vs.jalica.engine.model.*;
import de.uniks.vs.jalica.engine.authority.AllocationDifference;
import de.uniks.vs.jalica.engine.authority.CycleManager;
import de.uniks.vs.jalica.engine.authority.EntryPointAgentPair;
import de.uniks.vs.jalica.engine.constrainmodule.ConditionStore;
import de.uniks.vs.jalica.engine.teammanagement.view.AgentsInStateView;
import de.uniks.vs.jalica.engine.teammanagement.view.AssignmentView;

import java.util.*;
import java.util.concurrent.locks.Lock;

/**
 * Created by alex on 13.07.17.
 * Updated 21.6.19
 */
public class RunningPlan {

    private PlanStateTriple activeTriple;
    private PlanStatusInfo status;

    private ArrayList<RunningPlan> children;
    private RunningPlan parent;

    private BasicBehaviour basicBehaviour;

    private Assignment assignment;
    private CycleManager cycleManagement;
    private ConditionStore constraintStore;

    private PlanType planType;
    private boolean behaviour;

    private AlicaEngine alicaEngine;

    private LinkedHashMap<AbstractPlan, Integer> failedSubPlans;

    private Lock accessMutex;

    private static AlicaTime assignmentProtectionTime = AlicaTime.zero();


    public static void init(SystemConfig sc) {
        assignmentProtectionTime = new AlicaTime().inMilliseconds(Long.valueOf((String) sc.get("Alica").get("Alica.AssignmentProtectionTime")));
    }

    public RunningPlan(AlicaEngine ae){
        this.failedSubPlans = new LinkedHashMap<>();
        this.children = new ArrayList<>();
        this.activeTriple = new PlanStateTriple();
        this.status = new PlanStatusInfo();
        this.constraintStore = new ConditionStore();

        this.alicaEngine = ae;
        this.planType = null;
        this.behaviour = false;
        this.assignment = new Assignment();
        this.cycleManagement = new CycleManager(ae, this);
        this.basicBehaviour = null;
        this.parent = null;
    }

    public RunningPlan(AlicaEngine ae,  Plan plan) {
        this.failedSubPlans = new LinkedHashMap<>();
        this.children = new ArrayList<>();
        this.activeTriple = new PlanStateTriple();
        this.status = new PlanStatusInfo();
        this.constraintStore = new ConditionStore();

        this.alicaEngine = ae;
        this.planType = null;
        this.behaviour = false;
        this.assignment = new Assignment(plan);
        this.cycleManagement = new CycleManager(ae, this);
        this.basicBehaviour = null;
        this.parent = null;
        this.activeTriple.abstractPlan = plan;
    }

    public RunningPlan(AlicaEngine ae, PlanType pt) {
        this.failedSubPlans = new LinkedHashMap<>();
        this.children = new ArrayList<>();
        this.activeTriple = new PlanStateTriple();
        this.status = new PlanStatusInfo();
        this.constraintStore = new ConditionStore();

        this.alicaEngine = ae;
        this.planType = pt;
        this.behaviour = false;
        this.assignment = new Assignment();
        this.cycleManagement = new CycleManager(ae, this);
        this.basicBehaviour = null;
        this.parent = null;
    }

    public RunningPlan(AlicaEngine ae, Behaviour b) {
        this.failedSubPlans = new LinkedHashMap<>();
        this.children = new ArrayList<>();
        this.status = new PlanStatusInfo();
        this.constraintStore = new ConditionStore();

        this.alicaEngine = ae;
        this.planType = null;
        this.activeTriple = new PlanStateTriple(b, null, null);
        this.behaviour = true;
        this.assignment = new Assignment();
        this.cycleManagement = new CycleManager(ae, this);
        this.basicBehaviour =null;
        this.parent = null;
    }

    boolean isDeleteable() {
        if (!this.children.isEmpty()) {
            return false; // children deregister from their parents
        }
        if (this.status.active == PlanActivity.Activity.InActive) {
            return true; // shortcut for plans from planselector
        }
        return isRetired() && (!isBehaviour() || !this.alicaEngine.getBehaviourPool().isBehaviourRunningInContext(this));
    }

    void preTick() {
        if (isRetired()) {
            return;
        }
        evalRuntimeCondition();

        for (RunningPlan c : this.children) {
            c.preTick();
        }
    }

    PlanChange tick(RuleBook rules) {

        if (isRetired()) {
            return PlanChange.NoChange;
        }
        this.cycleManagement.update();
        PlanChange myChange = rules.visit(this);
        if (isRetired()) {
            return myChange;
        }
        PlanChange childChange = PlanChange.NoChange;
        // attention: do not use for each here: children are modified
        for (int i = 0; i < this.children.size(); i++) {
            RunningPlan rp = this.children.get(i);
            childChange = rules.updateChange(childChange, rp.tick(rules));
        }
        if (childChange != PlanChange.NoChange && childChange != PlanChange.InternalChange) {
            myChange = rules.updateChange(myChange, rules.visit(this));
        }
        return myChange;
    }

    public void setAllocationNeeded(boolean need) {
        this.status.allocationNeeded = need;
    }

    public boolean evalPreCondition() {
        if (this.activeTriple.abstractPlan == null) {
            CommonUtils.aboutError("Cannot Eval Condition, Plan is null");
            assert (false);
        }
        PreCondition preCondition = null;

        if (this.activeTriple.abstractPlan instanceof Behaviour) {
            Behaviour behaviour = (Behaviour) this.activeTriple.abstractPlan;
            preCondition = behaviour.getPreCondition();
        } else if (this.activeTriple.abstractPlan instanceof Plan) {
            Plan plan = (Plan) this.activeTriple.abstractPlan;
            preCondition = plan.getPreCondition();
        }
        if (preCondition == null) {
            return true;
        }
        try {
            return preCondition.evaluate(this);
        } catch (Exception e) {
            CommonUtils.aboutError("Exception in precondition: " + e.getMessage());
            return false;
        }
    }

    boolean evalRuntimeCondition() {

        if (this.activeTriple.abstractPlan == null) {
            CommonUtils.aboutError("Cannot Eval Condition, Plan is null");
            try {
                throw new Exception();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        RuntimeCondition runtimeCondition = null;
        if (this.activeTriple.abstractPlan instanceof Behaviour) {
            Behaviour behaviour = (Behaviour) this.activeTriple.abstractPlan;
            runtimeCondition = behaviour.getRuntimeCondition();
        } else if (this.activeTriple.abstractPlan instanceof Plan) {
            Plan plan = (Plan) this.activeTriple.abstractPlan;
            runtimeCondition = plan.getRuntimeCondition();
        }
        if (runtimeCondition == null) {
            this.status.runTimeConditionStatus = EvalStatus.True;
            return true;
        }
        try {
            boolean ret = runtimeCondition.evaluate(this);
            this.status.runTimeConditionStatus = (ret ? EvalStatus.True : EvalStatus.False);
            return ret;
        } catch (Exception e) {
            CommonUtils.aboutError("Exception in runtimecondition: " + this.activeTriple.abstractPlan.getName() + " " + e.getMessage());
            this.status.runTimeConditionStatus = EvalStatus.False;
            return false;
        }
    }

    public void addChildren(ArrayList<RunningPlan> runningPlans) {
        this.children.ensureCapacity(this.children.size() + runningPlans.size());

        for (RunningPlan r : runningPlans) {
            r.setParent(this);
            this.children.add(r);
            Integer value = this.failedSubPlans.get(r.getActivePlan());

            if (value != null) {
                r.status.failCount = value;
            }
            if (isActive()) {
                r.activate();
            }
        }
    }

    void removeChild(RunningPlan rp) {

        if(this.children.contains(rp)) {
            rp.parent = null ;
            this.children.remove(rp);
        }
    }

    void moveState( State nextState)
    {
        deactivateChildren();
        clearChildren();
        this.assignment.moveAllFromTo(this.activeTriple.entryPoint, this.activeTriple.state, nextState);
        useState(nextState);
        this.failedSubPlans.clear();
    }

    void printRecursive() {

        for ( RunningPlan c : this.children) {
            c.printRecursive();
        }

        if (this.children.isEmpty()) {
            System.out.println( "END CHILDREN of " + (this.activeTriple.abstractPlan == null ? "NULL" : this.activeTriple.abstractPlan.getName()));
        }
    }

    public void usePlan(AbstractPlan plan) {

        if (this.activeTriple.abstractPlan != plan) {
            this.status.planStartTime = this.alicaEngine.getAlicaClock().now();
            revokeAllConstraints();
            this.activeTriple.abstractPlan = plan;
            this.status.runTimeConditionStatus = EvalStatus.Unknown;
        }
    }

    public void useEntryPoint(EntryPoint value) {

        if (this.activeTriple.entryPoint != value) {
            ID mid = getOwnID();
            this.assignment.removeAgent(mid);
            this.activeTriple.entryPoint = value;

            if (value != null) {
                useState(value.getState());
                this.assignment.addAgent(mid, this.activeTriple.entryPoint, this.activeTriple.state);
            }
        }
    }

    public void useState(State s) {

        if (this.activeTriple.state != s) {
//            ALICA_ASSERT(s == nullptr || (_activeTriple.entryPoint && _activeTriple.entryPoint->isStateReachable(s))) //TODO: is this correct???
            assert(s == null || (this.activeTriple.entryPoint != null && this.activeTriple.entryPoint.isStateReachable(s)));

            System.err.println("RP("+this.getActivePlan().getName() + " " + this.hashCode() + "): change state " + (this.activeTriple.state != null ? this.activeTriple.state.getName(): "null") + "->"+ s.getName());
            this.activeTriple.state = s;
            this.status.stateStartTime = this.alicaEngine.getAlicaClock().now();

            if (s != null) {

                if (s.isFailureState()) {
                    this.status.status = PlanStatus.Status.Failed;
                } else if (s.isSuccessState()) {
                    ID mid = getOwnID();
                    this.assignment.getSuccessData(this.activeTriple.entryPoint).add(mid);
                    this.alicaEngine.getTeamManager().setSuccess(mid, this.activeTriple.abstractPlan, this.activeTriple.entryPoint);
                }
            }
        }
    }

    public PlanStatus.Status getStatus() {
        if (this.basicBehaviour != null) {
            if (this.basicBehaviour.isSuccess()) {
                return PlanStatus.Status.Success;
            } else if (this.basicBehaviour.isFailure()) {
                return PlanStatus.Status.Failed;
            } else {
                return PlanStatus.Status.Running;
            }
        }
        if (this.assignment.isSuccessful()) {
            return PlanStatus.Status.Success;
        }
        return this.status.status;
    }

    public void clearFailures() {
        this.status.failCount = 0;
    }

    public void clearFailedChildren() {
        this.failedSubPlans.clear();
    }

    public void addFailure() {
        ++this.status.failCount;
        setFailureHandlingNeeded(true);
    }

    public int getFailureCount() {
        return this.status.failCount;
    }

    public void deactivateChildren() {
        for (RunningPlan r : this.children) {
            r.deactivate();
        }
    }

    public void clearChildren() {
        for (RunningPlan r : this.children) {
        r.parent = null;
    }
        this.children.clear();
    }

    public void adaptAssignment( RunningPlan replacement) {
        this.assignment.adaptTaskChangesFrom(replacement.getAssignment());
     State newState = this.assignment.getStateOfAgent(getOwnID());

        boolean reactivate = false;

        if (this.activeTriple.state != newState) {
            this.status.active = PlanActivity.Activity.InActive;
            deactivateChildren();
            revokeAllConstraints();
            clearChildren();
            addChildren(replacement.getChildren());
            reactivate = true;
        } else {
            ArrayList<ID> robotsJoined = new ArrayList<>();
            this.assignment.getAgentsInState(newState, robotsJoined);
            for (RunningPlan c : this.children) {
                c.limitToRobots(robotsJoined);
            }
        }

        usePlan(replacement.getActivePlan());
        this.activeTriple.entryPoint = replacement.getActiveEntryPoint();
        useState(newState);
        if (reactivate) {
            activate();
        }
    }

    public void setFailedChild( AbstractPlan child) {
        Integer integer = this.failedSubPlans.get(child);

        if (integer != null) {
            this.failedSubPlans.replace(child, integer, (integer+1));
        } else {
            this.failedSubPlans.put(child, 1);
        }
    }

    void setFailureHandlingNeeded(boolean failHandlingNeeded) {
        if (failHandlingNeeded) {
            this.status.status = PlanStatus.Status.Failed;
        } else {
            if (this.status.status == PlanStatus.Status.Failed) {
                this.status.status = PlanStatus.Status.Running;
            }
        }
        this.status.failHandlingNeeded = failHandlingNeeded;
    }

    public void accept(IPlanTreeVisitor vis) {
        vis.visit(this);

        for (RunningPlan child : this.children) {
        assert(!child.isRetired());
        if (!child.isRetired()) {
            child.accept(vis);
        }
    }
    }

    void deactivate() {
        this.status.active = PlanActivity.Activity.Retired;
        if (isBehaviour()) {
            this.alicaEngine.getBehaviourPool().stopBehaviour(this);
        } else {
            this.alicaEngine.getTeamObserver().notifyAgentLeftPlan(this.activeTriple.abstractPlan);
        }
        revokeAllConstraints();
        deactivateChildren();
    }

    boolean isAnyChildStatus(PlanStatus.Status ps) {
        for (RunningPlan child : this.children) {
            assert (!child.isRetired());
            if (ps == child.getStatus()) {
                return true;
            }
        }
        return false;
    }

    boolean areAllChildrenStatus(PlanStatus.Status ps) {

        for (RunningPlan child : this.children) {
            assert (!child.isRetired());
            if (ps != child.getStatus()) {
                return false;
            }
        }
        // In case of a state, make sure that all children are actually running
        if (this.activeTriple.state != null) {
            return this.children.size() >= this.activeTriple.state.getPlans().size();
        }
        return true;
    }

   boolean isAnyChildTaskSuccessful() {
        for ( RunningPlan child : this.children) {
        if (child.isBehaviour()) {
            // Behaviours have no task status!
            continue;
        }
        if (child.getAssignment().isAnyTaskSuccessful()) {
            return true;
        }
    }

        return false;
    }

    void activate() {
        assert(this.status.active != PlanActivity.Activity.Retired);
        this.status.active = PlanActivity.Activity.Active;
        System.out.println("RP: activate plan " + this.getActivePlan().getName());
        if (isBehaviour()) {
            this.alicaEngine.getBehaviourPool().startBehaviour(this);
        }
        attachPlanConstraints();
        for (RunningPlan r : this.children) {
            r.activate();
        }
    }


    public void limitToRobots(ArrayList<ID> robots) {
        if (isBehaviour()) {
            return;
        }
        if (!this.cycleManagement.mayDoUtilityCheck()) {
            return;
        }

   boolean ownStateWasTouched = this.assignment.removeAllNotIn(robots, this.activeTriple.state);

        if (ownStateWasTouched) {
            for (RunningPlan c : this.children) {
                c.limitToRobots(robots);
            }
        }
    }

    void revokeAllConstraints()
    {
        this.constraintStore.clear();
    }

    void attachPlanConstraints() {
        if (this.activeTriple.abstractPlan instanceof Behaviour) {
            Behaviour behaviour = (Behaviour) this.activeTriple.abstractPlan;
            this.constraintStore.addCondition(behaviour.getPreCondition());
            this.constraintStore.addCondition(behaviour.getRuntimeCondition());
        }
        else if ( this.activeTriple.abstractPlan instanceof Plan) {
            Plan plan = (Plan) this.activeTriple.abstractPlan;
            this.constraintStore.addCondition(plan.getPreCondition());
            this.constraintStore.addCondition(plan.getRuntimeCondition());
        }
    }

   public boolean recursiveUpdateAssignment(ArrayList<SimplePlanTree> spts, ArrayList<ID> availableAgents, ArrayList<ID> noUpdates, AlicaTime now)
    {
        if (isBehaviour()) {
            return false;
        }
   boolean keepTask = this.status.planStartTime.time + assignmentProtectionTime.time > now.time;
   boolean keepState = this.status.stateStartTime.time + assignmentProtectionTime.time > now.time;
   boolean auth = this.cycleManagement.haveAuthority();

        // if keepTask, the task Assignment should not be changed!
       boolean ret = false;
        AllocationDifference aldif = this.cycleManagement.getNextDifference();
        for (SimplePlanTree spt : spts) {
        ID id = spt.getAgentID();
       boolean freezeAgent = keepState && this.assignment.getStateOfAgent(id) == getActiveState();
        if (freezeAgent) {
            continue;
        }
        if (spt.getState().getInPlan() != this.activeTriple.abstractPlan) { // the robot is no longer participating in this plan
            if (!keepTask && !auth) {
                EntryPoint ep = this.assignment.getEntryPointOfAgent(id);
                if (ep != null) {
                    this.assignment.removeAgentFrom(id, ep);
                    ret = true;
                    aldif.getSubtractions().add(new EntryPointAgentPair(ep, id));
                }
            }
        } else {
            if (keepTask || auth) { // Update only state, and that only if it is in the reachability graph of its
                // current entrypoint, else
                // ignore
                EntryPoint cep = this.assignment.getEntryPointOfAgent(id);
                if (cep != null) {
                    if (cep.isStateReachable(spt.getState())) {
                        this.assignment.setState(id, spt.getState(), cep);
                    }
                } else { // robot was not expected to be here during protected assignment time, add it.
                    this.assignment.addAgent(id, spt.getEntryPoint(), spt.getState());
                    aldif.getAdditions().add(new EntryPointAgentPair(spt.getEntryPoint(), id));
                }
            } else { // Normal Update
                EntryPoint ep = this.assignment.getEntryPointOfAgent(id);
                ret |= this.assignment.updateAgent(id, spt.getEntryPoint(), spt.getState());
                if (spt.getEntryPoint() != ep) {
                    aldif.getAdditions().add(new EntryPointAgentPair(spt.getEntryPoint(), id));
                    if (ep != null) {
                        aldif.getSubtractions().add(new EntryPointAgentPair(ep, id));
                    }
                }
            }
        }
    }

        ArrayList<ID> rem = new ArrayList<>();
        if (!keepTask) { // remove any robot no longer available in the spts (auth flag obey here, as robot might be
            // unavailable)
            // EntryPoint[] eps = this.Assignment.GetEntryPoints();
            ID ownId = getOwnID();

            for (int i = 0; i < this.assignment.getEntryPointCount(); ++i) {
            EntryPoint ep = this.assignment.getEntryPoint(i);
                rem.clear();
                AssignmentView robs = this.assignment.getAgentsWorking(i);

                for (ID rob : robs.get()) {

                    if (rob == ownId) {
                        continue;
                    }
               boolean freezeAgent = keepState && this.assignment.getStateOfAgent(rob) == getActiveState();

                    if (freezeAgent) {
                        continue;
                    }
                   boolean found = false;
                    if (noUpdates.contains(rob)) {
                        // found = true;
                        continue;
                    }
                    for (SimplePlanTree spt : spts) {
                        if (spt.getAgentID() == rob) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        rem.add(rob);
                        aldif.getSubtractions().add(new EntryPointAgentPair(ep, rob));
                        ret = true;
                    }
                }
                this.assignment.removeAllFrom(rem, ep);
            }
        }

        // enforce consistency between RA and PlanTree by removing robots deemed inactive:
        if (!auth) { // under authority do not remove robots from assignment
            for (int i = 0; i < this.assignment.getEntryPointCount(); ++i) {
            EntryPoint ep = this.assignment.getEntryPoint(i);
                rem.clear();
                AssignmentView robs = this.assignment.getAgentsWorking(i);
                for (ID rob : robs.get()) {
               boolean freezeAgent = keepState && this.assignment.getStateOfAgent(rob) == getActiveState();
                    if (freezeAgent) {
                        continue;
                    }
                    if (!availableAgents.contains(rob)) {
                        rem.add(rob);
                        aldif.getSubtractions().add(new EntryPointAgentPair(ep, rob));
                        ret = true;
                    }
                }
                this.assignment.removeAllFrom(rem, ep);
            }
        }

        aldif.setReason(AllocationDifference.Reason.message);

        // Update Success Collection:
        this.alicaEngine.getTeamObserver().updateSuccessCollection((Plan)getActivePlan(), this.assignment.getSuccessData());

        // If Assignment Protection Time for newly started plans is over, limit available robots to those in this active
        // state.

        ArrayList<ID> removableAgents = new ArrayList();
        if (!auth) {
            AgentsInStateView agentsJoined = this.assignment.getAgentsInState(getActiveState());

            for (ID iter : availableAgents) {

                if (!agentsJoined.get().contains(iter)) {
                    removableAgents.add(iter);
                }
//                else {
//                    ++iter;
//                }
            }
            for (ID id: removableAgents) {
                availableAgents.remove(id);
            }
            removableAgents.clear();
        } else { // in case of authority, remove all that are not assigned to same task
            AssignmentView agentsJoined = this.assignment.getAgentsWorking(getActiveEntryPoint());

            for (ID iter : availableAgents) {

                if (!agentsJoined.get().contains(iter)) {
                    removableAgents.add(iter);
                }
//                else {
//                    ++iter;
//                }
            }
            for (ID id: removableAgents) {
                availableAgents.remove(id);
            }
            removableAgents.clear();
        }
        // Give Plans to children
        for (RunningPlan r : this.children) {
        if (r.isBehaviour()) {
            continue;
        }
        ArrayList<SimplePlanTree> newcspts = new ArrayList<>();
        for (SimplePlanTree spt : spts) {
            if (spt.getState() == this.activeTriple.state) {
                for (SimplePlanTree cspt : spt.getChildren()) {
                    if (cspt.getState().getInPlan() == r.getActivePlan()) {
                        newcspts.add(cspt);
                        break;
                    }
                }
            }
        }
        ret |= r.recursiveUpdateAssignment(newcspts, availableAgents, noUpdates, now);
    }
        return ret;
    }

    void toMessage(ArrayList<Long> message, RunningPlan deepestNode, int depth, int curDepth) {

        if (isBehaviour() || isRetired()) {
            return;
        }
        if (this.activeTriple.state != null) {
            message.add(this.activeTriple.state.getID());
        } else {
            return;
        }
        if (curDepth > depth) {
            depth = curDepth;
            deepestNode = this;
        }
        if (this.children.size() > 0) {
            message.add(-1l);
            for ( RunningPlan r : this.children) {
                r.toMessage(message, deepestNode, depth, curDepth + 1);
            }
            message.add(-2l);
        }
    }

    @Override
    public String toString() {
        StringBuffer out = new StringBuffer();

        out.append("######## RP " + alicaEngine.getTeamManager().getLocalAgentID() + " ##########\n");
        PlanStateTriple ptz = getActiveTriple();
        out.append("Plan: " + (ptz.abstractPlan != null ? ptz.abstractPlan.getName() : "NULL") + "\n");
        out.append("PlanType: " + (getPlanType() != null ? getPlanType().getName() : "NULL") + "\n");
        out.append("ActState: " + (ptz.state != null ? ptz.state.getName() : "NULL") + "\n");
        out.append("Task: " + (ptz.entryPoint != null ? ptz.entryPoint.getTask().getName() : "NULL") + "\n");
        out.append("IsBehaviour: " + isBehaviour() + "\t");
        if (isBehaviour()) {
            out.append("Behaviour: " + (getBasicBehaviour() == null ? "NULL" : getBasicBehaviour().getName()) + "\n");
        }
        PlanStatusInfo psi = getStatusInfo();
        out.append("AllocNeeded: " + psi.allocationNeeded + "\n");
        out.append("FailHandlingNeeded: " + psi.failHandlingNeeded + "\t");
        out.append("FailCount: " + psi.failCount + "\n");
        out.append("Activity: " + PlanActivity.getPlanActivityName(psi.active) + "\n");
        out.append("Status: " + PlanStatus.getPlanStatusName(psi.status) + "\n");
        out.append("\n");
        if (!isBehaviour()) {
            out.append("Assignment: " + this.assignment);
        }
        out.append("Children: " + this.children.size());
        if (!this.children.isEmpty()) {
            out.append(" ( ");
            for (RunningPlan c : this.children) {
                if (c.activeTriple.abstractPlan == null) {
                    out.append("NULL PLAN, ");
                } else
                    out.append(c.activeTriple.abstractPlan.getName() + ", ");
            }
            out.append(")");
        }
        out.append("\nCycleManagement - Assignment Overridden: " + (this.cycleManagement.isOverridden() ? "true" : "false") + "\n");
        out.append("\n########## ENDRP " + alicaEngine.getTeamManager().getLocalAgentID() + " ###########" + "\n");
        return out.toString();
    }
    // -- getter setter --

    // Read/Write lock access, currently map to a single mutex
    // for future use already defined apart
    public ScopedReadLock getReadLock() { return new ScopedReadLock(this.accessMutex); }
    public ScopedWriteLock getWriteLock() { return new ScopedWriteLock(this.accessMutex); }

    public boolean isBehaviour()  { return this.behaviour; };
    public boolean isAllocationNeeded()  { return this.status.allocationNeeded; }
    public boolean isFailureHandlingNeeded()  { return this.status.failHandlingNeeded; }
    public AlicaTime getPlanStartTime()  { return this.status.planStartTime; }
    public AlicaTime getStateStartTime()  { return this.status.stateStartTime; }
    public boolean isActive()  { return this.status.active == PlanActivity.Activity.Active; }
    public boolean isRetired()  { return this.status.active == PlanActivity.Activity.Retired; }

    // Read/Write lock access, currently map to a single mutex
    // for future use already defined apart
//    ScopedReadLock getReadLock()  { return ScopedReadLock(this.accessMutex); }
//    ScopedWriteLock getWriteLock() { return ScopedWriteLock(this.accessMutex); }

    public ArrayList <RunningPlan> getChildren()  { return this.children; }
    public RunningPlan getParent()  { return this.parent; }

    public PlanType getPlanType()  { return this.planType; }

    public PlanStateTriple getActiveTriple()  { return this.activeTriple; }
    public PlanStatusInfo getStatusInfo()  { return this.status; }
    public State getActiveState()  { return this.activeTriple.state; }
    public EntryPoint getActiveEntryPoint()  { return this.activeTriple.entryPoint; }
    public AbstractPlan getActivePlan()  { return this.activeTriple.abstractPlan; }
    public Plan getActivePlanAsPlan()  { return isBehaviour() ? null  : (Plan) this.activeTriple.abstractPlan; }
    public Assignment getAssignment()  { return this.assignment; }
    public BasicBehaviour getBasicBehaviour()  { return this.basicBehaviour; }

    public void setParent(RunningPlan parent) { this.parent = parent; }
    public void setAssignment(Assignment assignment) { this.assignment = assignment; }
    public void setBasicBehaviour(BasicBehaviour basicBehaviour) { this.basicBehaviour = basicBehaviour; }

    public ConditionStore getConstraintStore()  { return this.constraintStore; }

    public CycleManager getCycleManagement()  { return this.cycleManagement; }

    // Temporary helper:
//    std::shared_ptr<RunningPlan> getSharedPointer() { return shared_from_this(); }

    public boolean isRuntimeConditionValid() {
        switch (this.status.runTimeConditionStatus) {
            case True:
                return true;
            case False:
                return false;
            case Unknown:
            default:
                return evalRuntimeCondition();
        }
    }

    ID getOwnID()  { return this.alicaEngine.getTeamManager().getLocalAgentID(); }
    public AlicaEngine getAlicaEngine()  { return this.alicaEngine; }
}





//
//    public RunningPlan(AlicaEngine ae, Plan plan) {
//        this(ae);
//        this.plan = plan;
//        // TODO: there is now usage -> remove it (java, c++)
////        transform(plan.getEntryPoints().begin(), plan.getEntryPoints().end(), back_inserter(epCol),
////                [](map<long, EntryPoint>::value_type& val)
////                            {
////                                return val.second;
////                            }
////        );
//
////        Vector<EntryPoint> epCol = new Vector<>();
////
////        for (EntryPoint e:  plan.getEntryPoints().values()) {
////            epCol.add(e);
////        }
////
////        Collections.sort(epCol);
////        this.assignment = new Assignment(0,eps,robots,plan);
//        this.behaviour = false;
//    }
//
//    public AbstractPlan getPlan() {
//        return plan;
//    }
//
//    public void setBasicBehaviour(BasicBehaviour basicBehaviour) {
//        this.basicBehaviour = basicBehaviour;
//    }
//
//    void toMessage(ArrayList<Long> message, RunningPlan deepestNode, int depth, int curDepth) {
//
//        if (this.isBehaviour()) {
//            return;
//        }
//
//        if (this.activeState != null) {
//            message.add(this.activeState.getID());
//        } else {
//            return;
//        }
//
//        if (curDepth > depth) {
//            depth = curDepth;
////            deepestNode = shared_from_this();
//        }
//
//        if (this.children.size() > 0) {
//            message.add(new Long(-1));
//
//            for (RunningPlan r : this.children) {
//                r.toMessage(message, deepestNode, depth, curDepth + 1);
//            }
//            message.add(new Long(-2));
//        }
//    }
//
//    public void printRecursive() {
//        CommonUtils.aboutNoImpl();
//    }
//
//    public void preTick() {
//
//        if(isRetired())
//            return;
//
//        evalRuntimeCondition();
//
//        for (RunningPlan plan :this.children) {
//            plan.preTick();
//        }
//    }
//
//    public PlanChange tick(RuleBook rules) {
////        if (isRetired()) {
////            return PlanChange.NoChange;
////        }
//        this.cycleManagement.update();
//
//        PlanChange myChange = rules.visit(this);
////        if (isRetired()) {
////            return myChange;
////        }
//        PlanChange childChange = PlanChange.NoChange;
//
//        //attention: do not use for each here: children are modified
//            // TODO: ??? but the list itself never changed (c++)
//
//        for (RunningPlan rp : this.children) {
//            childChange = rules.updateChange(childChange, rp.tick(rules));
//        }
//
//        if (childChange != PlanChange.NoChange && childChange != PlanChange.InternalChange) {
//            myChange = rules.updateChange(myChange, rules.visit(this));
//        }
//        return myChange;
//    }
//
//    public boolean isActive() {
//        return active;
//    }
//
//    public boolean isRetired() { /*return status.active == PlanActivity.Retired;*/ return true; }
//
//    public boolean isBehaviour() {
//        return behaviour;
//    }
//
////    public EntryPoint getOwnEntryPoint() { return ownEntryPoint; }
//    public EntryPoint getOwnEntryPoint() { return activeEntryPoint; }
//
//    public State getActiveState() {
//        return activeState;
//    }
//
//    public RunningPlan getParent() {return parent;}
//
//    public void setAllocationNeeded(boolean allocationNeeded) {
//        this.allocationNeeded = allocationNeeded;
//    }
//
//    public void moveState(State nextState) {
//        deactivateChildren();
//        clearChildren();
//        this.assignment.moveAgents(this.activeState, nextState);
//        this.setActiveState(nextState);
//        this.failedSubPlans.clear();
//    }
//
//    public void setActiveState(State state) {
//
//        if (CommonUtils.RP_DEBUG_debug) System.out.println("RP: set active state from " + (this.activeState != null? this.activeState.getName(): "null")
//                +" teamObserver " +state.getName());
//
//        if (this.activeState != state) {
//            this.activeState = state;
//            this.stateStartTime.time = alicaEngine.getAlicaClock().now().time;
//
//            if (this.activeState != null) {
//
//                if (this.activeState.isFailureState()) {
//                    this.status = PlanStatus.Failed;
//                }
//				else if (this.activeState.isSuccessState()) {
//                    this.assignment.getEpSuccessMapping().getAgents(this.activeEntryPoint).add(this.ownID);
//                    this.teamObserver.getOwnEngineData().getSuccessMarks().markSuccessfull(this.plan,
//                        this.activeEntryPoint);
//                }
//            }
//        }
//    }
//
//    public void clearChildren() {
//        this.children.clear();
//    }
//
//    public void deactivateChildren() {
//
//        for (RunningPlan r : this.children) {
//            r.deactivate();
//        }
//    }
//
//    private void deactivate() {
//        this.active = false;
//        if (this.isBehaviour())
//        {
////            behaviourPool.stopBehaviour(this.plan);
//            behaviourPool.stopBehaviour(this);
////            behaviourPool.stopBehaviour(shared_from_this());
//        }
//		else
//        {
//            this.teamObserver.notifyAgentLeftPlan(this.plan);
//        }
//        revokeAllConstraints();
//        deactivateChildren();
//    }
//
//    private void revokeAllConstraints() {
//        this.constraintStore.clear();
//    }
//
//    public ConditionStore getConstraintStore() {
//        return constraintStore;
//    }
//
//    public boolean getFailHandlingNeeded() {
//        return failHandlingNeeded;
//    }
//
//    public void setFailHandlingNeeded(boolean failHandlingNeeded) {
//        this.failHandlingNeeded = failHandlingNeeded;
//    }
//
//    public void clearFailures() {
//        this.failCount = 0;
//    }
//
//    public void setOwnEntryPoint(EntryPoint value)
//    {
//        if (this.activeEntryPoint != value)
//        {
//            this.assignment.removeAgent(ownID);
//            this.activeEntryPoint = value;
//            if (this.activeEntryPoint != null)
//            {
//                this.setActiveState(this.activeEntryPoint.getState());
//                this.assignment.addAgent(ownID, this.activeEntryPoint, this.activeState);
//            }
//        }
//    }
//
//    public void setAgentsAvail(ArrayList<Long> agents)
//    {
//        this.agentsAvail.clear();
//        this.agentsAvail = CommonUtils.move(agents);
//    }
//
//    public Assignment getAssignment() {
//        return assignment;
//    }
//
//    public void clearFailedChildren() {
//        this.failedSubPlans.clear();
//    }
//
//    public boolean isAllocationNeeded() {
//        return allocationNeeded;
//    }
//
//    public void setPlan(AbstractPlan plan) {
//        this.plan = plan;
//    }
//
//    public void setParent(RunningPlan parent) {
//        this.parent = parent;
//    }
//
//    public PlanType getPlanType() {
//        return planType;
//    }
//
//    public void setAssignment(Assignment assignment) {
//        this.assignment = assignment;
//    }
//
//    public boolean evalPreCondition() {
//        if (this.plan == null)
//        {
//            System.err.println("Cannot Eval Condition, Plan is null" );
//            try {
//                throw new Exception();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        if (this.plan.getPreCondition() == null)
//        {
//            return true;
//        }
//        try
//        {
//            return this.plan.getPreCondition().evaluate(this);
//        }
//        catch (Exception  e)
//        {
//            System.err.println("Exception in precondition: " + e.getMessage() );
//            return false;
//        }
//    }
//
//    public boolean evalRuntimeCondition() {
//        if (this.plan == null)
//        {
//            System.err.println( "Cannot Eval Condition, Plan is null" );
//            try {
//                throw new Exception();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        if (this.plan.getRuntimeCondition() == null)
//        {
//            return true;
//        }
//        try
//        {
//            return this.plan.getRuntimeCondition().evaluate(this);
//        }
//        catch (Exception e)
//        {
//            System.err.println("Exception in runtimecondition: " + this.plan.getName() + e.getMessage() );
//            return false;
//        }
//    }
//
//    public void addChildren(ArrayList<RunningPlan> runningPlans) {
//
//        for (RunningPlan r : runningPlans) {
//            r.setParent(this);
//            this.children.add(r);
//
//            if (this.failedSubPlans.containsKey(r.getPlan())) {
//                r.failCount = this.failedSubPlans.get(r.getPlan());
//            }
//
//            if (this.active) {
//                r.activate();
//            }
//        }
//    }
//
//    protected void activate() {
//        this.active = true;
//
//        if (this.isBehaviour()) {
//            behaviourPool.startBehaviour(this);
//        }
//
//        this.attachPlanConstraints();
//        for (RunningPlan r : this.children) {
//            r.activate();
//        }
//    }
//
//    private void attachPlanConstraints() {
//        //		cout << "RP: attachPlanConstraints " << this.getPlan().getName() << endl;
//        this.constraintStore.addCondition(this.plan.getPreCondition());
//        this.constraintStore.addCondition(this.plan.getRuntimeCondition());
//    }
//
//    public CycleManager getCycleManagement() {
//        return cycleManagement;
//    }
//
//    public ArrayList<RunningPlan> getChildren() {
//        return children;
//    }
//
//    public void limitToAgents(Set<Long> agents) {
//        if (this.isBehaviour())
//        {
//            return;
//        }
//        if (!this.cycleManagement.mayDoUtilityCheck())
//        {
//            return;
//        }
//        boolean recurse = false;
//        Vector<Long> curentAgents = this.assignment.getAllAgents();
//        for (long r : curentAgents)
//        {
//            if (CommonUtils.find(curentAgents,0, curentAgents.size()-1, r) == curentAgents.lastElement())
//            {
//                if (this.activeState != null
//                    && this.assignment.getAgentStateMapping().getStateOfAgent(r) == this.activeState)
//                {
//                    recurse = true;
//                }
//                this.assignment.removeAgent(r);
//            }
//        }
//        if (recurse)
//        {
//            for (RunningPlan c : this.children)
//            {
//                c.limitToAgents(agents);
//            }
//        }
//    }
//
//    public void adaptAssignment(RunningPlan r) {
//        State newState = r.getAssignment().getAgentStateMapping().getState(this.ownID);
//        r.getAssignment().getAgentStateMapping().reconsiderOldAssignment(this.assignment, r.getAssignment());
//        boolean reactivate = false;
//
//        if (this.activeState != newState)
//        {
//            this.active = false;
//            this.deactivateChildren();
//            this.revokeAllConstraints();
//            this.clearChildren();
//            this.addChildren(r.getChildren());
//            reactivate = true;
//        }
//		else
//        {
//            Set<Long> agentsJoined = r.getAssignment().getAgentStateMapping().getAgentsInState(newState);
//            for (RunningPlan r1 : this.children)
//            {
//                r1.limitToAgents(agentsJoined);
//            }
//        }
//
//        this.plan = r.getPlan();
//        this.activeEntryPoint = r.getOwnEntryPoint();
//        this.assignment = r.assignment;
//        this.setActiveState(newState);
//        if (reactivate)
//        {
//            this.activate();
//        }
//
//    }
//
//    public void addFailure() {
//        this.failCount++;
//        this.failHandlingNeeded = true;
//    }
//
//    public PlanStatus getStatus() {
//        if (this.basicBehaviour != null)
//        {
//            if (this.basicBehaviour.isSuccess())
//            {
//                System.out.println("RP: " + this.plan.getName() + " BEH Success" );
//                return PlanStatus.Success;
//            }
//			else if (this.basicBehaviour.isFailure())
//            {
//                System.out.println("RP: " + this.plan.getName() + " BEH Failed");
//                return PlanStatus.Failed;
//            }
//			else
//            {
//                System.out.println("RP: " + this.plan.getName() + " BEH Running");
//                return PlanStatus.Running;
//            }
//        }
//        if (this.assignment != null && this.assignment.isSuccessfull())
//        {
//            //cout << "RP: " << this.plan.getName() << " ASS Success" << endl;
//            return PlanStatus.Success;
//        }
//        //cout << "RP: " << this.plan.getName() << " STATUS " << (this.status == PlanStatus::Running ? "RUNNING" : (this.status == PlanStatus::Success ? "SUCCESS" : "FAILED")) << endl;
//        return this.status;
//    }
//
//    public int getFailure() {
//        return this.failCount;
//    }
//
//    public void setFailedChild(AbstractPlan child) {
//
//        // TODO: fix work around
//        Integer last = 0;
//        for (AbstractPlan key : failedSubPlans.keySet()) {
//            last = failedSubPlans.get(key);
//        }
//
//        if (this.failedSubPlans.get(child) != last)
//        {
//            Integer intChild = this.failedSubPlans.get(child);
//            this.failedSubPlans.put(child,intChild++);
//        }
//		else
//        {
//            this.failedSubPlans.put(child, 1);
//        }
//    }
////
////    public long getId() {
////        return id;
////    }
//
//    public AlicaEngine getAlicaEngine() {
//        return alicaEngine;
//    }
//
//    public boolean recursiveUpdateAssignment(ArrayList<SimplePlanTree> spts, ArrayList<Long> availableAgents,
//                                             ArrayList<Long> noUpdates, AlicaTime now) {
//
//        if (this.isBehaviour()) {
//            return false;
//        }
//
//        boolean keepTask = ((this.planStartTime.time + assignmentProtectionTime.time) < now.time);
//        boolean authority = this.cycleManagement.haveAuthority();
//
//        //if keepTask, the task Assignment should not be changed!
//        boolean result = false;
//        AllocationDifference allocationDifference = new AllocationDifference();
//
//        for (SimplePlanTree simplePlanTree : spts) {
//
//            if (simplePlanTree.getState().getInPlan() != this.plan) { //the robot is no longer participating in this plan
//
//                if (!keepTask & !authority) {
//                    EntryPoint entryPoint = this.getAssignment().getEntryPointOfAgent(simplePlanTree.getAgentID());
//
//                    if (entryPoint != null ) {
//                        this.getAssignment().removeAgent(simplePlanTree.getAgentID());
//                        result = true;
//                        allocationDifference.getSubtractions().add(new EntryPointAgentPair(entryPoint, simplePlanTree.getAgentID()));
//                    }
//                }
//            }
//			else {
//
//                if (keepTask || authority) { //Update only state, and that only if it is in the reachability graph of its current entrypoint, else ignore
//                    EntryPoint cep = this.getAssignment().getEntryPointOfAgent(simplePlanTree.getAgentID());
//
//                    if (cep != null ) {
//
//                        if (!cep.getReachableStates().contains(simplePlanTree.getState())) {
//                            this.getAssignment().getAgentStateMapping().setState(simplePlanTree.getAgentID(), simplePlanTree.getState());
//                        }
//                    }
//                    else { //robot was not expected teamObserver be here during protected assignment time, add it.
//                        this.getAssignment().addAgent(simplePlanTree.getAgentID(), simplePlanTree.getEntryPoint(), simplePlanTree.getState());
//                        allocationDifference.getAdditions().add(
//                                        new EntryPointAgentPair(simplePlanTree.getEntryPoint(), simplePlanTree.getAgentID()));
//
//                    }
//                }
//                else
//                { //Normal Update
//                    EntryPoint entryPoint = this.getAssignment().getEntryPointOfAgent(simplePlanTree.getAgentID());
//                    result |= this.getAssignment().updateAgent(simplePlanTree.getAgentID(), simplePlanTree.getEntryPoint(), simplePlanTree.getState());
//
//                    if (simplePlanTree.getEntryPoint() != entryPoint) {
//                        allocationDifference.getAdditions().add(new EntryPointAgentPair(simplePlanTree.getEntryPoint(), simplePlanTree.getAgentID()));
//
//                        if (entryPoint != null )
//                            allocationDifference.getSubtractions().add(new EntryPointAgentPair(entryPoint, simplePlanTree.getAgentID()));
//                    }
//
//                }
//            }
//        }
//        ArrayList<Long> agents = new ArrayList<>();
//
//        if (!keepTask) { //remove any robot no longer available in the spts (auth flag obey here, as robot might be unavailable)
//            //EntryPoint[] eps = this.Assignment.GetEntryPoints();
//            EntryPoint entryPoint;
//
//            for (int i = 0; i < this.getAssignment().getEntryPointCount(); i++) {
//                entryPoint = this.getAssignment().getEpAgentsMapping().getEntryPoint(i);
//                agents.clear();
//                Vector<Long> agentsWorking = this.getAssignment().getAgentsWorking(entryPoint);
//
//                for (long agentID : (agentsWorking)) {
//
//                    if (agentID == ownID)
//                        continue;
//                    boolean found = false;
//
//                    if (noUpdates.contains(agentID)) {
////                        found = true;
//                        continue;
//                    }
//
//                    for (SimplePlanTree simplePlanTree : spts) {
//
//                        if (simplePlanTree.getAgentID() == agentID) {
//                            found = true;
//                            break;
//                        }
//                    }
//
//                    if (!found) {
//                        agents.add(agentID);
//                        //this.Assignment.RemoveRobot(agentID);
//                        allocationDifference.getSubtractions().add(new EntryPointAgentPair(entryPoint, agentID));
//                        result = true;
//                    }
//                }
//
//                for (long agentID : agents) {
//                    this.getAssignment().removeAgent(agentID, entryPoint);
//                }
//            }
//        }
//
//        //enforce consistency between RA and PlanTree by removing agents deemed inactive:
//        if (!authority) { //under authority do not remove agents from assignment
//            EntryPoint entryPoint;
//
//            for (int i = 0; i < this.getAssignment().getEntryPointCount(); i++) {
//                entryPoint = this.getAssignment().getEpAgentsMapping().getEntryPoint(i);
//                agents.clear();
//                Vector<Long> agentsWorking = this.getAssignment().getAgentsWorking(entryPoint);
//
//                for (long agentID : (agentsWorking)) {
//
//                    if (agentID==ownID)
//                        continue;
//
//                    if (!availableAgents.contains(agentID)) {
//                        agents.add(agentID);
//                        //this.Assignment.RemoveRobot(rob);
//                        allocationDifference.getSubtractions().add(new EntryPointAgentPair(entryPoint, agentID));
//                        result = true;
//                    }
//                }
//
//                for (long rob : agents) {
//                    this.getAssignment().removeAgent(rob, entryPoint);
//                }
//            }
//        }
//
//        allocationDifference.setReason(AllocationDifference.Reason.message);
//        this.cycleManagement.setNewAllocDiff(allocationDifference);
////Update Success Collection:
//        this.teamObserver.updateSuccessCollection((Plan)this.getPlan(), this.getAssignment().getEpSuccessMapping());
//
////If Assignment Protection Time for newly started plans is over, limit available agents teamObserver those in this active state.
//        if (this.stateStartTime.time + assignmentProtectionTime.time > now.time) {
//            Set<Long> agentsJoined = this.getAssignment().getAgentStateMapping().getAgentsInState(this.getActiveState());
//
//            for (int i = 0; i < availableAgents.size(); i++) {
//
//                if (!agentsJoined.contains(availableAgents.get(i))) {
//
//                    availableAgents.remove(availableAgents.get(i));
//                    i--;
//                }
//            }
//        }
//		else if (authority)
//    { // in case of authority, remove all that are not assigned teamObserver same task
//        Vector<Long> agentsJoined = this.getAssignment().getAgentsWorking(this.getOwnEntryPoint());
//
//        if (agentsJoined != null) {
//
//            for (int i = 0; i < availableAgents.size(); i++) {
//
//                if (!agentsJoined.contains(availableAgents.get(i)) ) {
//                    availableAgents.remove(availableAgents.get(i));
//                    i--;
//                }
//            }
//        }
//    }
//        //Give Plans teamObserver children
//        for (RunningPlan r : this.children) {
//
//            if (r.isBehaviour()) {
//                continue;
//            }
//            ArrayList<SimplePlanTree > newcspts = new ArrayList<>();
//
//            for (SimplePlanTree spt : spts) {
//
//                if (spt.getState() == this.activeState) {
//
//                    for (SimplePlanTree cspt : spt.getChildren()) {
//
//                        if (cspt.getState().getInPlan() == r.getPlan()) {
//                            newcspts.add(cspt);
//                            break;
//                        }
//                    }
//                }
//            }
//            result |= r.recursiveUpdateAssignment(newcspts, availableAgents, noUpdates, now);
//        }
//        return result;
//    }
//
//    public long getOwnID() {return ownID;}
//
//    @Override
//    public String toString() {
//        String ss = "######## RP ##########" + "\n";
//        ss += "Plan: " + (plan != null ? plan.getName() : "NULL") + "\n";
//        ss += "Parent: " + (getParent() != null && getParent().getPlan() != null ? getParent().getPlan().getName(): "NULL") + "\n";
//        ss += "PlanType: " + (planType != null ? planType.getName() : "NULL") + "\n";
//        ss += "ActState: " + (activeState != null ? activeState.getName() : "NULL") + "\n";
//        ss += "Task: " + (this.getOwnEntryPoint() != null ? this.getOwnEntryPoint().getTask().getName() : "NULL") + "\n";
//        ss += "IsBehaviour: " + this.isBehaviour() + "\t";
//
//        if (this.isBehaviour()) {
//            ss += "Behaviour: " + (this.basicBehaviour == null ? "NULL" : this.basicBehaviour.getName()) + "\n";
//        }
//        ss += "AllocNeeded: " + this.allocationNeeded + "\n";
//        ss += "FailHandlingNeeded: " + this.failHandlingNeeded + "\t";
//        ss += "FailCount: " + this.failCount + "\n";
//        ss += "IsActive: " + this.active + "\n";
//        ss += "Status: " + (this.status == PlanStatus.Running ? "RUNNING" : (this.status == PlanStatus.Success ? "SUCCESS" : "FAILED")) + "\n";
//        ss += "AvailRobots: ";
//
//        for (long r : (this.agentsAvail)) {
//            ss += " " + r;
//        }
//        ss += "\n";
//
//        if (this.assignment != null) {
//            ss += "Assignment:" + this.assignment.toString();
//        }
//		else
//            ss += "Assignment is null." + "\n";
//        ss += "Children: " + this.children.size();
//
//        if (this.children.size() > 0) {
//            ss += " ( ";
//
//            for (RunningPlan r : this.children) {
//
//                if (r.plan == null) {
//                    ss += "NULL PLAN, ";
//                }
//                else
//                    ss += r.plan.getName() + ", ";
//            }
//            ss += ")";
//        }
//        ss += "\n" + "CycleManagement - Assignment Overridden: "
//                + (this.getCycleManagement().isOverridden() ? "true" : "false") + "\n";
//        ss += "\n########## ENDRP ###########" + "\n";
//        return ss;
//    }
//
//    public boolean anyChildrenStatus(PlanStatus success) {
//        CommonUtils.aboutNoImpl();
//        return false;
//    }
//
//    public AlicaTime getStateStartTime() {
//        return stateStartTime;
//    }
//
//    public AbstractPlan getActivePlan() { return this.activeTriple.abstractPlan; }
//
//}
