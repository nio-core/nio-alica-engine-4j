package de.uniks.vs.jalica.engine.planselection;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.*;
import de.uniks.vs.jalica.engine.idmanagement.ID;
import de.uniks.vs.jalica.engine.model.*;
import de.uniks.vs.jalica.engine.taskassignment.TaskAssignment;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by alex on 13.07.17.
 * Updated 22.6.19
 */
public class PlanSelector {

    public static int POOL_SIZE = 10100;

    private PartialAssignmentPool pap;
    private ITeamObserver to;
    private AlicaEngine ae;
    private PlanBase pb;

    public PlanSelector(AlicaEngine ae, PlanBase pb) {
        this.pap = new PartialAssignmentPool(POOL_SIZE);
        this.ae = ae;
        this.to = ae.getTeamObserver();
        this.pb = pb;
        assert(this.ae != null && this.to != null && this.pb != null);
    }


    RunningPlan getBestSimilarAssignment(RunningPlan rp) {
        // GET ROBOTS TO ASSIGN
        ArrayList<ID>  robots = new ArrayList<>();
        rp.getAssignment().getAllAgents(robots);
        DoubleWrapper oldUtil = new DoubleWrapper(0);
        return getBestSimilarAssignment(rp, robots, oldUtil);
    }

    public RunningPlan getBestSimilarAssignment(RunningPlan rp, ArrayList<ID> robots, DoubleWrapper currentUtility) {
        assert(!rp.isBehaviour());
        // Reset set index of the partial assignment object pool
        this.pap.reset();
        try {
            if (rp.getPlanType() == null) {
                return createRunningPlan(rp.getParent(), new ArrayList<Plan>(Arrays.asList((Plan)rp.getActivePlan())), robots, rp, null, currentUtility);
            } else {
                return createRunningPlan(rp.getParent(), rp.getPlanType().getPlans(), robots, rp, rp.getPlanType(), currentUtility);
            }
        } catch (RuntimeException e) {
            CommonUtils.aboutError(e.getMessage());
            this.pap.increaseSize();
            return null;
        }
    }

    public boolean getPlansForState(RunningPlan planningParent, ArrayList<AbstractPlan> plans, ArrayList<ID> robotIDs, ArrayList<RunningPlan> o_plans)
    {
        this.pap.reset();
        try {
            return getPlansForStateInternal(planningParent, plans, robotIDs, o_plans);
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            this.pap.increaseSize();
            return false;
        }
    }

    RunningPlan createRunningPlan(RunningPlan planningParent,ArrayList<Plan> plans, ArrayList<ID> robotIDs, RunningPlan oldRp,
        PlanType relevantPlanType, DoubleWrapper oldUtility)
    {
        ArrayList<Plan> newPlanList = new ArrayList<>();
        // REMOVE EVERY PLAN WITH TOO GREAT MIN CARDINALITY
        for (Plan plan : plans) {
            // CHECK: number of robots < minimum cardinality of this plan
            if (plan.getMinCardinality() > ((robotIDs.size()) + this.to.successesInPlan(plan))) {
                System.out.println("PS: AgentIds: " + robotIDs + "\n"
                        + "= " + robotIDs.size() + " IDs are not enough for the plan " + plan.getName() + "!");
            } else {
                // this plan was ok according to its cardinalities, so we can add it
                newPlanList.add(plan);
            }
        }
        // WE HAVE NOT ENOUGH ROBOTS TO EXECUTE ANY PLAN
        if (newPlanList.isEmpty()) {
            return null;
        }

        TaskAssignment ta = new TaskAssignment(this.ae, newPlanList, robotIDs, this.pap);
        Assignment oldAss = null;
        RunningPlan rp;
        if (oldRp == null) {
            // preassign other robots, because we dont need a similar assignment
            rp = this.pb.makeRunningPlan(relevantPlanType);
            ta.preassignOtherAgents();
        } else {
            if (!oldRp.getAssignment().isValid() || !oldRp.isRuntimeConditionValid()) {
                oldUtility.value = -1.0;
            } else {
                assert (!oldRp.isBehaviour());
                PartialAssignment ptemp = this.pap.getNext();
                Plan oldPlan = (Plan) oldRp.getActivePlan();
                ptemp.prepare(oldPlan, ta);
                oldRp.getAssignment().fillPartial(ptemp);
                oldUtility.value = oldPlan.getUtilityFunction().eval(ptemp, oldRp.getAssignment()).getMax();
            }
            // dont preassign other robots, because we need a similar assignment (not the same)
            rp = this.pb.makeRunningPlan(oldRp.getPlanType());
            oldAss = oldRp.getAssignment();
        }

        // some variables for the do while loop
        ID localAgentID = this.ae.getTeamManager().getLocalAgentID();
        // PLANNINGPARENT
        rp.setParent(planningParent);
        ArrayList<RunningPlan> rpChildren =  new ArrayList<>();
        boolean found = false;
        ArrayList<ID> agents = new ArrayList<>();

        do {
            rpChildren.clear();
            // ASSIGNMENT
            rp.setAssignment(ta.getNextBestAssignment(oldAss));

            if (rp.getAssignment().getPlan() == null) {
               System.out.println("PS: no good assignment found.");
                return null;
            }
            // PLAN (needed for Conditionchecks)
            rp.usePlan(rp.getAssignment().getPlan());

           System.out.print("PS: rp.Assignment of Plan " + rp.getActivePlan().getName() + " is: " + rp.getAssignment());

            // CONDITIONCHECK
            if (!rp.evalPreCondition()) {
                continue;
            }
            if (!rp.isRuntimeConditionValid()) {
                continue;
            }

            // OWN ENTRYPOINT
            EntryPoint ep = rp.getAssignment().getEntryPointOfAgent(localAgentID);

            if (ep == null) {
               System.out.println("PS: The agent "
                        + "(Id: " + localAgentID + ") is not assigned to enter the plan " + rp.getActivePlan().getName() + " and will IDLE!");

                rp.useState(null);
                rp.useEntryPoint(null);
                return rp; // If we return here, this robot will idle (no ep at rp)
            } else {
                // assign found EntryPoint (this robot dont idle)
                rp.useEntryPoint(ep);
            }
            // ACTIVE STATE set by RunningPlan
            if (oldRp == null) {
                // RECURSIVE PLANSELECTING FOR NEW STATE
                agents.clear();
                rp.getAssignment().getAgentsWorking(ep, agents);

                found = getPlansForStateInternal(rp, rp.getActiveState().getPlans(), agents, rpChildren);
            } else {
               System.out.println("PS: no recursion due to utilitycheck");
                // Don't calculate children, because we have an
                // oldRp -> we just replace the oldRp
                // (not its children -> this will happen in an extra call)
                break;
            }
        } while (!found);
        // WHEN WE GOT HERE, THIS ROBOT WONT IDLE AND WE HAVE A
        // VALID ASSIGNMENT, WHICH PASSED ALL RUNTIME CONDITIONS
        if (found && !rpChildren.isEmpty()) // c# rpChildren != null
        {
           System.out.println("PS: Set child -> parent reference");
            rp.addChildren(rpChildren);
        }

       System.out.println("PS: Created RunningPlan: \n" + rp);

        return rp; // If we return here, this agent is normal assigned
    }


    private boolean getPlansForStateInternal(RunningPlan planningParent,  ArrayList<AbstractPlan> plans,  ArrayList<ID> robotIDs, ArrayList<RunningPlan> runningPlans) {
        System.out.println("<######PS: GetPlansForState: Parent:" + (planningParent != null ? planningParent.getActivePlan().getName() : "null")
                                                           + " plan count: " + plans.size() + " agent count: " + robotIDs.size() + " ######>");
        for (AbstractPlan ap : plans) {

            if (ap instanceof Behaviour) {
                Behaviour beh = (Behaviour) ap;
                RunningPlan rp = this.pb.makeRunningPlan(beh);
                // A Behaviour is a Plan too (in this context)
                rp.usePlan(beh);
                runningPlans.add(rp);
                rp.setParent(planningParent);

                System.out.println("PS: Added Behaviour " + beh.getName());

            } else if (ap instanceof Plan) {
                Plan p = (Plan) ap;
                DoubleWrapper zeroValue = new DoubleWrapper(0);
                RunningPlan rp = createRunningPlan(planningParent, new ArrayList<>(Arrays.asList(p)), robotIDs, null, null, zeroValue);

                if (rp == null) {
                    System.out.println("PS: It was not possible to create a RunningPlan for the Plan " + p.getName() + "!");
                    return false;
                }
                runningPlans.add(rp);
            } else if (ap instanceof PlanType) {
                PlanType pt = (PlanType) ap;
                DoubleWrapper zeroVal = new DoubleWrapper(0);
                RunningPlan rp = createRunningPlan(planningParent, pt.getPlans(), robotIDs, null, pt, zeroVal);
                if (rp == null) {
                    System.out.println("PS: It was not possible to create a RunningPlan for the Plan " + pt.getName() + "!");
                    return false;
                }
                runningPlans.add(rp);
            }
        }
        return true;
    }


//    @Override
//    public ArrayList<RunningPlan> getPlansForState(RunningPlan planningParent,
//                                                   ArrayList<AbstractPlan> plans, Vector<Long> agents) {
//        PartialAssignment.reset(partialAssignmentPool);
//        ArrayList<RunningPlan> newPlans = this.getPlansForStateInternal(planningParent, plans, agents);
//        return newPlans;
//    }
//
//    @Override
//    public RunningPlan getBestSimilarAssignment(RunningPlan runningPlan, Vector<Long> agents) {
//        // Reset set index of the partial assignment multiton
//        PartialAssignment.reset(partialAssignmentPool);
//        // CREATE NEW PLAN LIST
//        ArrayList<Plan> newPlanList;
//
//        if (runningPlan.getPlanType() == null) {
//            newPlanList = new ArrayList<Plan>();
//            newPlanList.add((Plan) runningPlan.getPlan());
//        } else {
//            newPlanList = runningPlan.getPlanType().getPlans();
//        }
//        // GET AGENTS TO ASSIGN
//        Vector<Long> selectedAgents = runningPlan.getAssignment().getAllAgents();
//        return this.createRunningPlan(runningPlan.getParent(), newPlanList, selectedAgents, runningPlan, runningPlan.getPlanType());
//    }
//
//    private ArrayList<RunningPlan> getPlansForStateInternal(RunningPlan planningParent, ArrayList<AbstractPlan> abstractPlans, Vector<Long> agentIDs) {
//        ArrayList<RunningPlan> rps = new ArrayList<RunningPlan>();
//
//        if (CommonUtils.PS_DEBUG_debug) System.out.println("###### PS::getPlansForState  State:" + planningParent.getActiveState().getName() +"  Parent:"
//                + (planningParent != null ? planningParent.getPlan().getName() : "null") + " plan count: "
//                + abstractPlans.size() + " agent count: "
//                + agentIDs.size() + " ######" );
//
//        RunningPlan runningPlan;
//        ArrayList<Plan> plans;
//        BehaviourConfiguration behaviourConfiguration;
//        Plan plan;
//        PlanType planType;
//        PlanningProblem planningProblem;
//
//        for (AbstractPlan abstractPlan : abstractPlans) {
//            // BEHAVIOUR CONFIGURATION
//            if (abstractPlan == null) {
//                System.out.println("PS: plan is null");
//                continue;
//            }
//
//            if (abstractPlan instanceof BehaviourConfiguration) {
//                behaviourConfiguration = (BehaviourConfiguration) abstractPlan;
//                runningPlan = new RunningPlan(alicaEngine, behaviourConfiguration);
//                // A BehaviourConfiguration is a Plan too (in this context)
//                runningPlan.setPlan(behaviourConfiguration);
//                rps.add(runningPlan);
//                runningPlan.setParent(planningParent);
//                if (CommonUtils.PS_DEBUG_debug) System.out.println("PS: Added Behaviour " + behaviourConfiguration.getBehaviour().getName() );
//            } else {
//                // PLAN
//                if ((abstractPlan instanceof Plan)) {
//                    plan = (Plan)abstractPlan;
//                    plans = new ArrayList<>();
//                    plans.add(plan);
//                    runningPlan = this.createRunningPlan(planningParent, plans, agentIDs, null, null);
//
//                    if (runningPlan == null) {
//                        if (CommonUtils.PS_DEBUG_debug) System.out.println("PS: It was not possible teamObserver create a RunningPlan for the Plan " + plan.getName() + " !");
//                        return null;
//                    }
//                    rps.add(runningPlan);
//                }
//                else
//                {
//                    // PLANTYPE
////                    pt = dynamic_cast<PlanType*>(ap);
////                    if (pt != null)
//                    if (abstractPlan instanceof PlanType)
//                    {
//                        planType = (PlanType)(abstractPlan);
//                        runningPlan = this.createRunningPlan(planningParent, planType.getPlans(), agentIDs, null, planType);
//                        if (runningPlan == null)
//                        {
//                            if (CommonUtils.PS_DEBUG_debug) System.out.println( "PS: It was not possible teamObserver create a RunningPlan for the Plan (Plantype) " + planType.getName()
//                                    + "!" );
//                            return null;
//                        }
//                        rps.add(runningPlan);
//                    }
//                    else
//                    {
//                        planningProblem = null;
////                        pp = dynamic_cast<PlanningProblem*>(ap);
////                        if (pp == null)
//                        if (!(abstractPlan instanceof PlanningProblem))
//                        {
//                            System.err.println( "PS: WTF? An AbstractPlan wasnt a BehaviourConfiguration, a Plan, a PlanType nor a PlannigProblem: " + abstractPlan.getID() );
//                            try {
//                                throw new Exception();
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        else
//                            planningProblem = (PlanningProblem)abstractPlan;
//
//                        //TODO implement method in planner
//                        Plan myP = alicaEngine.getPlanner().requestPlan(planningProblem);
//                        plans = new ArrayList<Plan>();
//                        plans.add(myP);
//                        runningPlan = this.createRunningPlan(planningParent, plans, agentIDs, null, null);
//                        if (runningPlan == null)
//                        {
//                            if (CommonUtils.PS_DEBUG_debug) System.out.println( "PS: Unable teamObserver execute planning result" );
//                            return null;
//                        }
//                        rps.add(runningPlan);
//                    }
//                }// else Plan
//            }// else BehaviourConfiguration
//        }// foreach AbstractPlan
//        return rps;
//    }
//
//    private RunningPlan createRunningPlan(RunningPlan planningParent, ArrayList<Plan> plans,
//                                          Vector<Long> agentIDs, RunningPlan oldRunningPlan, PlanType relevantPlanType) {
//        ArrayList<Plan> inputPlans = new ArrayList<>();
//
//        // REMOVE EVERY PLAN WITH TOO GREAT MIN CARDINALITY
//        for (Plan plan : plans) {
//            // CHECK: number of agents < minimum cardinality of this plan
//            if (plan.getMinCardinality() > (agentIDs.size() + teamObserver.successesInPlan(plan))) {
//                if (CommonUtils.PS_DEBUG_debug) System.out.println("PS: AgentIds: " + agentIDs +" \n= " + agentIDs.size() + " IDs are not enough for the plan " + plan.getName() + "!");
//            } else {
//                // this plan was ok according teamObserver its cardinalities, so we can add it
//                inputPlans.add(plan);
//            }
//        }
//
//        // WE HAVE NOT ENOUGH AGENTS TO EXECUTE ANY PLAN
//        if (inputPlans.isEmpty())
//            return null;
//
//        // TASKASSIGNMENT
//        TaskAssignment ta;
//        RunningPlan rp;
//        Assignment oldAssignment = null;
//
//        if (oldRunningPlan == null) {
//            // preassign other agents, because we dont need a similar assignment
//            rp = planBase.makeRunningPlan(relevantPlanType);
////            rp = new RunningPlan(alicaEngine, relevantPlanType);
//            ta = new TaskAssignment(this.alicaEngine.getPartialAssignmentPool(), this.alicaEngine.getTeamObserver(), inputPlans, agentIDs, true);
//        }
//        else {
//            // dont preassign other agents, because we need a similar assignment (not the same)
//            //TODO:implement validation
//            rp = planBase.makeRunningPlan(oldRunningPlan.getPlanType());
////            rp = new RunningPlan(alicaEngine, oldRp.getPlanType());
//            ta = new TaskAssignment(this.alicaEngine.getPartialAssignmentPool(), this.alicaEngine.getTeamObserver(), inputPlans, agentIDs, false);
//            oldAssignment = oldRunningPlan.getAssignment();
//        }
//
//        // some variables for the do while loop
//        EntryPoint ep = null;
//        AgentProperties ownAgentProb = teamObserver.getOwnAgentProperties();
//        // PLANNINGPARENT
//        rp.setParent(planningParent);
//        ArrayList<RunningPlan> rpChildren = new ArrayList<>();
//
//        do
//        {
//            rpChildren.clear();
//            // ASSIGNMENT
//            rp.setAssignment(ta.getNextBestAssignment(oldAssignment));
//
//            if (rp.getAssignment() == null)
//            {
////#ifdef PSDEBUG
//                if (CommonUtils.PS_DEBUG_debug) System.out.println( "PS: rp.Assignment is NULL" );
////#endif
//                return null;
//            }
//
//            // PLAN (needed for Conditionchecks)
//            rp.setPlan(rp.getAssignment().getPlan());
////#ifdef PSDEBUG
//            if (CommonUtils.PS_DEBUG_debug) System.out.println( "PS: rp.Assignment of Plan " + rp.getPlan().getName() + " from " + ownAgentProb.extractID() + " is: " + rp.getAssignment().toString());
////#endif
//            // CONDITIONCHECK
//            if (!rp.evalPreCondition())
//            {
//                continue;
//            }
//            if (!rp.evalRuntimeCondition())
//            {
//                continue;
//            }
//
//            // OWN ENTRYPOINT
//            ep = rp.getAssignment().getEntryPointOfAgent(ownAgentProb.extractID());
//
//            if (ep == null)
//            {
////#ifdef PSDEBUG
//                if (CommonUtils.PS_DEBUG_debug) System.out.println( "PS: The agent " + ownAgentProb.getName() + "(Id: " + ownAgentProb.extractID()
//                        + ") is not assigned teamObserver enter the plan " + rp.getPlan().getName() + " and will IDLE!");
////#endif
//                rp.setActiveState(null);
//                rp.setOwnEntryPoint(null);
//                return rp; // If we return here, this agent will idle (no ep at rp)
//            }
//            else
//            {
//                // assign found EntryPoint (this agent dont idle)
//                rp.setOwnEntryPoint(ep);
//            }
//
//            // ACTIVE STATE set by RunningPlan
//            if(oldRunningPlan == null)
//            {
//                // RECURSIVE PLANSELECTING FOR NEW STATE
//                rpChildren = this.getPlansForStateInternal(rp, rp.getActiveState().getPlans(), rp.getAssignment().getAgentsWorking(ep));
//            }
//            else
//            {
//                if (CommonUtils.PS_DEBUG_debug) System.out.println( "PS: no recursion due teamObserver utilitycheck" );
//                // Don't calculate children, because we have an
//                // oldRp . we just replace the oldRp
//                // (not its children . this will happen in an extra call)
//                break;
//            }
//        } while (rpChildren == null);
//        // WHEN WE GOT HERE, THIS AGENT WONT IDLE AND WE HAVE A
//        // VALID ASSIGNMENT, WHICH PASSED ALL RUNTIME CONDITIONS
//        if(rpChildren != null && rpChildren.size() != 0) // c# rpChildren != null
//        {
//            if (CommonUtils.PS_DEBUG_debug) System.out.println( "PS: Set child . father reference");
//            rp.addChildren(rpChildren);
//        }
//        if (CommonUtils.PS_DEBUG_debug) System.out.println( "PS: Created RunningPlan: \n" + rp.toString() );
//        ta = null;
//        return rp; // If we return here, this agent is normal assigned
//    }
//
//
//    public void setPlanBase(PlanBase planBase) {
//        this.planBase = planBase;
//    }
}
