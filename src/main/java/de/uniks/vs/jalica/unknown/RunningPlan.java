package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.behaviours.BasicBehaviour;
import de.uniks.vs.jalica.behaviours.BehaviourConfiguration;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.teamobserver.ITeamObserver;

import java.util.*;

/**
 * Created by alex on 13.07.17.
 */
public class RunningPlan {

    private int failCount;
    private long ownID;
    //    private long id;
    private boolean active;
    private boolean behaviour;
    private boolean failHandlingNeeded;
    private boolean allocationNeeded;

    private AbstractPlan plan;
    private AlicaEngine alicaEngine;
    private AlicaTime planStartTime;
    private AlicaTime stateStartTime;
    private Assignment assignment;
    private BasicBehaviour basicBehaviour;
    private BehaviourConfiguration behaviourConfiguration;
    private ConditionStore constraintStore;
    private CycleManager cycleManagement;
    private EntryPoint activeEntryPoint;
    private IBehaviourPool behaviourPool;
    private ITeamObserver teamObserver;
    private PlanStatus status;
    private PlanType planType;
    private RunningPlan parent;
    private State activeState;
    //    private EntryPoint ownEntryPoint;

    private ArrayList<RunningPlan> children;
    private ArrayList<Long> agentsAvail;
    private LinkedHashMap<AbstractPlan, Integer> failedSubPlans;

    protected AlicaTime assignmentProtectionTime;

    RunningPlan(AlicaEngine alicaEngine) {

        failedSubPlans = new LinkedHashMap<>();
        agentsAvail = new ArrayList<>();
        children = new VerboseArrayList<>();

        this.assignmentProtectionTime = new AlicaTime(Long.valueOf((String) alicaEngine.getSystemConfig().get("Alica").get("Alica.AssignmentProtectionTime")) * 1000000);
        this.alicaEngine = alicaEngine;
        this.behaviour = false;
        this.planStartTime = new AlicaTime(0);
        this.stateStartTime = new AlicaTime(0);
        this.teamObserver = alicaEngine.getTeamObserver();
        this.ownID = teamObserver.getOwnID();
        this.status = PlanStatus.Running;
        this.failCount = 0;
        this.active = false;
        this.allocationNeeded = false;
        this.failHandlingNeeded = false;
        this.constraintStore = new ConditionStore();
        this.cycleManagement = new CycleManager(alicaEngine, this);
        this.agentsAvail = new ArrayList<>();
    }

    public RunningPlan(AlicaEngine ae, PlanType pt) {
        this(ae);
        this.planType = pt;
        this.behaviour = false;
    }

    public RunningPlan(AlicaEngine ae, BehaviourConfiguration bc) {
        this(ae);
        this.behaviourConfiguration = bc;
        this.plan = bc;
        this.behaviourPool = ae.getBehaviourPool();
        this.behaviour = true;
    }

    public RunningPlan(AlicaEngine ae, Plan plan) {
        this(ae);
        this.plan = plan;
        // TODO: there is now usage -> remove it (java, c++)
//        transform(plan.getEntryPoints().begin(), plan.getEntryPoints().end(), back_inserter(epCol),
//                [](map<long, EntryPoint>::value_type& val)
//                            {
//                                return val.second;
//                            }
//        );

//        Vector<EntryPoint> epCol = new Vector<>();
//
//        for (EntryPoint e:  plan.getEntryPoints().values()) {
//            epCol.add(e);
//        }
//
//        Collections.sort(epCol);
//        this.assignment = new Assignment(0,eps,robots,plan);
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
            message.add(this.activeState.getID());
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
        this.cycleManagement.update();

        PlanChange myChange = rules.visit(this);
        PlanChange childChange = PlanChange.NoChange;

        //attention: do not use for each here: children are modified
            // TODO: ??? but the list itself never changed (c++)

        for (RunningPlan rp : this.children) {
            childChange = rules.updateChange(childChange, rp.tick(rules));
        }

        if (childChange != PlanChange.NoChange && childChange != PlanChange.InternalChange) {
            myChange = rules.updateChange(myChange, rules.visit(this));
        }
        return myChange;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isBehaviour() {
        return behaviour;
    }

//    public EntryPoint getOwnEntryPoint() { return ownEntryPoint; }
    public EntryPoint getOwnEntryPoint() { return activeEntryPoint; }

    public State getActiveState() {
        return activeState;
    }

    public RunningPlan getParent() {return parent;}

    public void setAllocationNeeded(boolean allocationNeeded) {
        this.allocationNeeded = allocationNeeded;
    }

    public void moveState(State nextState) {
        deactivateChildren();
        clearChildren();
        this.assignment.moveAgents(this.activeState, nextState);
        this.setActiveState(nextState);
        this.failedSubPlans.clear();
    }

    public void setActiveState(State state) {

        System.out.println("RP: set active state from " + (this.activeState != null? this.activeState.name: "null") +" to " +state.name);

        if (this.activeState != state) {
            this.activeState = state;
            this.stateStartTime.time = alicaEngine.getIAlicaClock().now().time;

            if (this.activeState != null) {

                if (this.activeState.isFailureState()) {
                    this.status = PlanStatus.Failed;
                }
				else if (this.activeState.isSuccessState()) {
                    this.assignment.getEpSuccessMapping().getAgents(this.activeEntryPoint).add(this.ownID);
                    this.teamObserver.getOwnEngineData().getSuccessMarks().markSuccessfull(this.plan,
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
//            behaviourPool.stopBehaviour(this.plan);
            behaviourPool.stopBehaviour(this);
//            behaviourPool.stopBehaviour(shared_from_this());
        }
		else
        {
            this.teamObserver.notifyAgentLeftPlan(this.plan);
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
            this.assignment.removeAgent(ownID);
            this.activeEntryPoint = value;
            if (this.activeEntryPoint != null)
            {
                this.setActiveState(this.activeEntryPoint.getState());
                this.assignment.addAgent(ownID, this.activeEntryPoint, this.activeState);
            }
        }
    }

    public void setAgentsAvail(ArrayList<Long> agents)
    {
        this.agentsAvail.clear();
        this.agentsAvail = CommonUtils.move(agents);
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

        for (RunningPlan r : runningPlans) {
            r.setParent(this);
            this.children.add(r);

            if (this.failedSubPlans.containsKey(r.getPlan())) {
                r.failCount = this.failedSubPlans.get(r.getPlan());
            }

            if (this.active) {
                r.activate();
            }
        }
    }

    protected void activate() {
        this.active = true;

        if (this.isBehaviour()) {
            behaviourPool.startBehaviour(this);
        }

        this.attachPlanConstraints();
        for (RunningPlan r : this.children) {
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

    public void limitToAgents(Set<Long> agents) {
        if (this.isBehaviour())
        {
            return;
        }
        if (!this.cycleManagement.mayDoUtilityCheck())
        {
            return;
        }
        boolean recurse = false;
        Vector<Long> curentAgents = this.assignment.getAllAgents();
        for (long r : curentAgents)
        {
            if (CommonUtils.find(curentAgents,0, curentAgents.size()-1, r) == curentAgents.lastElement())
            {
                if (this.activeState != null
                    && this.assignment.getAgentStateMapping().stateOfAgent(r) == this.activeState)
                {
                    recurse = true;
                }
                this.assignment.removeAgent(r);
            }
        }
        if (recurse)
        {
            for (RunningPlan c : this.children)
            {
                c.limitToAgents(agents);
            }
        }
    }

    public void adaptAssignment(RunningPlan r) {
        State newState = r.getAssignment().getAgentStateMapping().getState(this.ownID);
        r.getAssignment().getAgentStateMapping().reconsiderOldAssignment(this.assignment, r.getAssignment());
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
            Set<Long> agentsJoined = r.getAssignment().getAgentStateMapping().getAgentsInState(newState);
            for (RunningPlan r1 : this.children)
            {
                r1.limitToAgents(agentsJoined);
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
                System.out.println("RP: " + this.plan.getName() + " BEH Success" );
                return PlanStatus.Success;
            }
			else if (this.basicBehaviour.isFailure())
            {
                System.out.println("RP: " + this.plan.getName() + " BEH Failed");
                return PlanStatus.Failed;
            }
			else
            {
                System.out.println("RP: " + this.plan.getName() + " BEH Running");
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
//
//    public long getId() {
//        return id;
//    }

    public AlicaEngine getAlicaEngine() {
        return alicaEngine;
    }

    public boolean recursiveUpdateAssignment(ArrayList<SimplePlanTree> spts, Vector<Long> availableAgents,
                                             ArrayList<Long> noUpdates, AlicaTime now) {

        if (this.isBehaviour()) {
            return false;
        }

        boolean keepTask = ((this.planStartTime.time + assignmentProtectionTime.time) < now.time);
        boolean auth = this.cycleManagement.haveAuthority();

        //if keepTask, the task Assignment should not be changed!
        boolean ret = false;
        AllocationDifference aldif = new AllocationDifference();

        for (SimplePlanTree spt : spts) {

            if (spt.getState().getInPlan() != this.plan) { //the robot is no longer participating in this plan

                if (!keepTask & !auth) {
                    EntryPoint ep = this.getAssignment().getEntryPointOfAgent(spt.getAgentID());

                    if (ep != null ) {
                        this.getAssignment().removeAgent(spt.getAgentID());
                        ret = true;
                        aldif.getSubtractions().add(new EntryPointAgentPair(ep, spt.getAgentID()));
                    }
                }
            }
			else {

                if (keepTask || auth) { //Update only state, and that only if it is in the reachability graph of its current entrypoint, else ignore
                    EntryPoint cep = this.getAssignment().getEntryPointOfAgent(spt.getAgentID());

                    if (cep != null ) {

                        if (!cep.getReachableStates().contains(spt.getState())) {
                            this.getAssignment().getAgentStateMapping().setState(spt.getAgentID(), spt.getState());
                        }
                    }
                    else { //robot was not expected teamObserver be here during protected assignment time, add it.
                        this.getAssignment().addAgent(spt.getAgentID(), spt.getEntryPoint(), spt.getState());
                        aldif.getAdditions().add(
                                        new EntryPointAgentPair(spt.getEntryPoint(), spt.getAgentID()));

                    }
                }
                else
                { //Normal Update
                    EntryPoint ep = this.getAssignment().getEntryPointOfAgent(spt.getAgentID());
                    ret |= this.getAssignment().updateAgent(spt.getAgentID(), spt.getEntryPoint(), spt.getState());

                    if (spt.getEntryPoint() != ep) {
                        aldif.getAdditions().add(new EntryPointAgentPair(spt.getEntryPoint(), spt.getAgentID()));

                        if (ep != null )
                            aldif.getSubtractions().add(new EntryPointAgentPair(ep, spt.getAgentID()));
                    }

                }
            }
        }
        ArrayList<Long> rem = new ArrayList<>();

        if (!keepTask) { //remove any robot no longer available in the spts (auth flag obey here, as robot might be unavailable)
            //EntryPoint[] eps = this.Assignment.GetEntryPoints();
            EntryPoint ep;

            for (int i = 0; i < this.getAssignment().getEntryPointCount(); i++) {
                ep = this.getAssignment().getEpAgentsMapping().getEp(i);
                rem.clear();
                Vector<Long> robs = this.getAssignment().getAgentsWorking(ep);

                for (long rob : (robs)) {

                    if (rob == ownID)
                        continue;
                    boolean found = false;

                    if (noUpdates.contains(rob)) {
                        //found = true;
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
                        //this.Assignment.RemoveRobot(rob);
                        aldif.getSubtractions().add(new EntryPointAgentPair(ep, rob));
                        ret = true;
                    }
                }

                for (long rob : rem) {
                    this.getAssignment().removeAgent(rob, ep);
                }
            }
        }

        //enforce consistency between RA and PlanTree by removing agents deemed inactive:
        if (!auth) { //under authority do not remove agents from assignment
            EntryPoint ep;

            for (int i = 0; i < this.getAssignment().getEntryPointCount(); i++) {
                ep = this.getAssignment().getEpAgentsMapping().getEp(i);
                rem.clear();
                Vector<Long> robs = this.getAssignment().getAgentsWorking(ep);

                for (long rob : (robs)) {
                    //if (rob==ownID) continue;

                    if (!availableAgents.contains(rob)) {
                        rem.add(rob);
                        //this.Assignment.RemoveRobot(rob);
                        aldif.getSubtractions().add(new EntryPointAgentPair(ep, rob));
                        ret = true;
                    }
                }

                for (long rob : rem) {
                    this.getAssignment().removeAgent(rob, ep);
                }
            }
        }

        aldif.setReason(AllocationDifference.Reason.message);
        this.cycleManagement.setNewAllocDiff(aldif);
//Update Success Collection:
        this.teamObserver.updateSuccessCollection((Plan)this.getPlan(), this.getAssignment().getEpSuccessMapping());

//If Assignment Protection Time for newly started plans is over, limit available agents teamObserver those in this active state.
        if (this.stateStartTime.time + assignmentProtectionTime.time > now.time) {
            Set<Long> agentsJoined = this.getAssignment().getAgentStateMapping().getAgentsInState(this.getActiveState());

            for (int i = 0; i < availableAgents.size(); i++) {

                if (!agentsJoined.contains(availableAgents.get(i))) {

                    availableAgents.remove(availableAgents.get(i));
                    i--;
                }
            }
        }
		else if (auth)
    { // in case of authority, remove all that are not assigned teamObserver same task
        Vector<Long> agentsJoined = this.getAssignment().getAgentsWorking(this.getOwnEntryPoint());

        if (agentsJoined != null) {

            for (int i = 0; i < availableAgents.size(); i++) {

                if (!agentsJoined.contains(availableAgents.get(i)) ) {
                    availableAgents.remove(availableAgents.get(i));
                    i--;
                }
            }
        }
    }
        //Give Plans teamObserver children
        for (RunningPlan r : this.children) {

            if (r.isBehaviour()) {
                continue;
            }
            ArrayList<SimplePlanTree > newcspts = new ArrayList<>();

            for (SimplePlanTree spt : spts) {

                if (spt.getState() == this.activeState) {

                    for (SimplePlanTree cspt : spt.getChildren()) {

                        if (cspt.getState().getInPlan() == r.getPlan()) {
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

    public long getOwnID() {return ownID;}

    @Override
    public String toString() {
        String ss = "######## RP ##########" + "\n";
        ss += "Plan: " + (plan != null ? plan.getName() : "NULL") + "\n";
        ss += "Parent: " + (getParent() != null && getParent().getPlan() != null ? getParent().getPlan().getName(): "NULL") + "\n";
        ss += "PlanType: " + (planType != null ? planType.getName() : "NULL") + "\n";
        ss += "ActState: " + (activeState != null ? activeState.getName() : "NULL") + "\n";
        ss += "Task: " + (this.getOwnEntryPoint() != null ? this.getOwnEntryPoint().getTask().getName() : "NULL") + "\n";
        ss += "IsBehaviour: " + this.isBehaviour() + "\t";

        if (this.isBehaviour()) {
            ss += "Behaviour: " + (this.basicBehaviour == null ? "NULL" : this.basicBehaviour.getName()) + "\n";
        }
        ss += "AllocNeeded: " + this.allocationNeeded + "\n";
        ss += "FailHandlingNeeded: " + this.failHandlingNeeded + "\t";
        ss += "FailCount: " + this.failCount + "\n";
        ss += "IsActive: " + this.active + "\n";
        ss += "Status: " + (this.status == PlanStatus.Running ? "RUNNING" : (this.status == PlanStatus.Success ? "SUCCESS" : "FAILED")) + "\n";
        ss += "AvailRobots: ";

        for (long r : (this.agentsAvail)) {
            ss += " " + r;
        }
        ss += "\n";

        if (this.assignment != null) {
            ss += "Assignment:" + this.assignment.toString();
        }
		else
            ss += "Assignment is null." + "\n";
        ss += "Children: " + this.children.size();

        if (this.children.size() > 0) {
            ss += " ( ";

            for (RunningPlan r : this.children) {

                if (r.plan == null) {
                    ss += "NULL PLAN, ";
                }
                else
                    ss += r.plan.getName() + ", ";
            }
            ss += ")";
        }
        ss += "\n" + "CycleManagement - Assignment Overridden: "
                + (this.getCycleManagement().isOverridden() ? "true" : "false") + "\n";
        ss += "\n########## ENDRP ###########" + "\n";
        return ss;
    }

    public boolean anyChildrenStatus(PlanStatus success) {
        CommonUtils.aboutNoImpl();
        return false;
    }

    public AlicaTime getStateStartTime() {
        return stateStartTime;
    }
}
