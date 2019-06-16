package de.uniks.vs.jalica.engine.planselection;

import de.uniks.vs.jalica.engine.*;
import de.uniks.vs.jalica.engine.model.*;
import de.uniks.vs.jalica.engine.collections.AgentProperties;
import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.common.PlanningProblem;
import de.uniks.vs.jalica.engine.taskassignment.TaskAssignment;

import javax.sound.midi.Soundbank;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by alex on 13.07.17.
 */
public class PlanSelector implements IPlanSelector {
    private PartialAssignmentPool partialAssignmentPool;
    private ITeamObserver teamObserver;
    private AlicaEngine alicaEngine;
    private PlanBase planBase;

    public PlanSelector(AlicaEngine alicaEngine, PlanBase planBase, PartialAssignmentPool assignmentPool) {
        this.alicaEngine = alicaEngine;
        this.planBase = planBase;
        this.teamObserver = this.alicaEngine.getTeamObserver();
        this.partialAssignmentPool = assignmentPool;
    }

    @Override
    public ArrayList<RunningPlan> getPlansForState(RunningPlan planningParent,
                                                   ArrayList<AbstractPlan> plans, Vector<Long> agents) {
        PartialAssignment.reset(partialAssignmentPool);
        ArrayList<RunningPlan> newPlans = this.getPlansForStateInternal(planningParent, plans, agents);
        return newPlans;
    }

    @Override
    public RunningPlan getBestSimilarAssignment(RunningPlan runningPlan, Vector<Long> agents) {
        // Reset set index of the partial assignment multiton
        PartialAssignment.reset(partialAssignmentPool);
        // CREATE NEW PLAN LIST
        ArrayList<Plan> newPlanList;

        if (runningPlan.getPlanType() == null) {
            newPlanList = new ArrayList<Plan>();
            newPlanList.add((Plan) runningPlan.getPlan());
        } else {
            newPlanList = runningPlan.getPlanType().getPlans();
        }
        // GET AGENTS TO ASSIGN
        Vector<Long> selectedAgents = runningPlan.getAssignment().getAllAgents();
        return this.createRunningPlan(runningPlan.getParent(), newPlanList, selectedAgents, runningPlan, runningPlan.getPlanType());
    }

    private ArrayList<RunningPlan> getPlansForStateInternal(RunningPlan planningParent, ArrayList<AbstractPlan> abstractPlans, Vector<Long> agentIDs) {
        ArrayList<RunningPlan> rps = new ArrayList<RunningPlan>();

        if (CommonUtils.PS_DEBUG_debug) System.out.println("###### PS::getPlansForState  State:" + planningParent.getActiveState().getName() +"  Parent:"
                + (planningParent != null ? planningParent.getPlan().getName() : "null") + " plan count: "
                + abstractPlans.size() + " agent count: "
                + agentIDs.size() + " ######" );

        RunningPlan runningPlan;
        ArrayList<Plan> plans;
        BehaviourConfiguration behaviourConfiguration;
        Plan plan;
        PlanType planType;
        PlanningProblem planningProblem;

        for (AbstractPlan abstractPlan : abstractPlans) {
            // BEHAVIOUR CONFIGURATION
            if (abstractPlan == null) {
                System.out.println("PS: plan is null");
                continue;
            }

            if (abstractPlan instanceof BehaviourConfiguration) {
                behaviourConfiguration = (BehaviourConfiguration) abstractPlan;
                runningPlan = new RunningPlan(alicaEngine, behaviourConfiguration);
                // A BehaviourConfiguration is a Plan too (in this context)
                runningPlan.setPlan(behaviourConfiguration);
                rps.add(runningPlan);
                runningPlan.setParent(planningParent);
                if (CommonUtils.PS_DEBUG_debug) System.out.println("PS: Added Behaviour " + behaviourConfiguration.getBehaviour().getName() );
            } else {
                // PLAN
                if ((abstractPlan instanceof Plan)) {
                    plan = (Plan)abstractPlan;
                    plans = new ArrayList<>();
                    plans.add(plan);
                    runningPlan = this.createRunningPlan(planningParent, plans, agentIDs, null, null);

                    if (runningPlan == null) {
                        if (CommonUtils.PS_DEBUG_debug) System.out.println("PS: It was not possible teamObserver create a RunningPlan for the Plan " + plan.getName() + " !");
                        return null;
                    }
                    rps.add(runningPlan);
                }
                else
                {
                    // PLANTYPE
//                    pt = dynamic_cast<PlanType*>(ap);
//                    if (pt != null)
                    if (abstractPlan instanceof PlanType)
                    {
                        planType = (PlanType)(abstractPlan);
                        runningPlan = this.createRunningPlan(planningParent, planType.getPlans(), agentIDs, null, planType);
                        if (runningPlan == null)
                        {
                            if (CommonUtils.PS_DEBUG_debug) System.out.println( "PS: It was not possible teamObserver create a RunningPlan for the Plan (Plantype) " + planType.getName()
                                    + "!" );
                            return null;
                        }
                        rps.add(runningPlan);
                    }
                    else
                    {
                        planningProblem = null;
//                        pp = dynamic_cast<PlanningProblem*>(ap);
//                        if (pp == null)
                        if (!(abstractPlan instanceof PlanningProblem))
                        {
                            System.err.println( "PS: WTF? An AbstractPlan wasnt a BehaviourConfiguration, a Plan, a PlanType nor a PlannigProblem: " + abstractPlan.getID() );
                            try {
                                throw new Exception();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else
                            planningProblem = (PlanningProblem)abstractPlan;

                        //TODO implement method in planner
                        Plan myP = alicaEngine.getPlanner().requestPlan(planningProblem);
                        plans = new ArrayList<Plan>();
                        plans.add(myP);
                        runningPlan = this.createRunningPlan(planningParent, plans, agentIDs, null, null);
                        if (runningPlan == null)
                        {
                            if (CommonUtils.PS_DEBUG_debug) System.out.println( "PS: Unable teamObserver execute planning result" );
                            return null;
                        }
                        rps.add(runningPlan);
                    }
                }// else Plan
            }// else BehaviourConfiguration
        }// foreach AbstractPlan
        return rps;
    }

    private RunningPlan createRunningPlan(RunningPlan planningParent, ArrayList<Plan> plans,
                                          Vector<Long> agentIDs, RunningPlan oldRunningPlan, PlanType relevantPlanType) {
        ArrayList<Plan> inputPlans = new ArrayList<>();

        // REMOVE EVERY PLAN WITH TOO GREAT MIN CARDINALITY
        for (Plan plan : plans) {
            // CHECK: number of agents < minimum cardinality of this plan
            if (plan.getMinCardinality() > (agentIDs.size() + teamObserver.successesInPlan(plan))) {
                if (CommonUtils.PS_DEBUG_debug) System.out.println("PS: AgentIds: " + agentIDs +" \n= " + agentIDs.size() + " IDs are not enough for the plan " + plan.getName() + "!");
            } else {
                // this plan was ok according teamObserver its cardinalities, so we can add it
                inputPlans.add(plan);
            }
        }

        // WE HAVE NOT ENOUGH AGENTS TO EXECUTE ANY PLAN
        if (inputPlans.isEmpty())
            return null;

        // TASKASSIGNMENT
        TaskAssignment ta;
        RunningPlan rp;
        Assignment oldAssignment = null;

        if (oldRunningPlan == null) {
            // preassign other agents, because we dont need a similar assignment
            rp = planBase.makeRunningPlan(relevantPlanType);
//            rp = new RunningPlan(alicaEngine, relevantPlanType);
            ta = new TaskAssignment(this.alicaEngine.getPartialAssignmentPool(), this.alicaEngine.getTeamObserver(), inputPlans, agentIDs, true);
        }
        else {
            // dont preassign other agents, because we need a similar assignment (not the same)
            //TODO:implement validation
            rp = planBase.makeRunningPlan(oldRunningPlan.getPlanType());
//            rp = new RunningPlan(alicaEngine, oldRp.getPlanType());
            ta = new TaskAssignment(this.alicaEngine.getPartialAssignmentPool(), this.alicaEngine.getTeamObserver(), inputPlans, agentIDs, false);
            oldAssignment = oldRunningPlan.getAssignment();
        }

        // some variables for the do while loop
        EntryPoint ep = null;
        AgentProperties ownAgentProb = teamObserver.getOwnAgentProperties();
        // PLANNINGPARENT
        rp.setParent(planningParent);
        ArrayList<RunningPlan> rpChildren = new ArrayList<>();

        do
        {
            rpChildren.clear();
            // ASSIGNMENT
            rp.setAssignment(ta.getNextBestAssignment(oldAssignment));

            if (rp.getAssignment() == null)
            {
//#ifdef PSDEBUG
                if (CommonUtils.PS_DEBUG_debug) System.out.println( "PS: rp.Assignment is NULL" );
//#endif
                return null;
            }

            // PLAN (needed for Conditionchecks)
            rp.setPlan(rp.getAssignment().getPlan());
//#ifdef PSDEBUG
            if (CommonUtils.PS_DEBUG_debug) System.out.println( "PS: rp.Assignment of Plan " + rp.getPlan().getName() + " from " + ownAgentProb.extractID() + " is: " + rp.getAssignment().toString());
//#endif
            // CONDITIONCHECK
            if (!rp.evalPreCondition())
            {
                continue;
            }
            if (!rp.evalRuntimeCondition())
            {
                continue;
            }

            // OWN ENTRYPOINT
            ep = rp.getAssignment().getEntryPointOfAgent(ownAgentProb.extractID());

            if (ep == null)
            {
//#ifdef PSDEBUG
                if (CommonUtils.PS_DEBUG_debug) System.out.println( "PS: The agent " + ownAgentProb.getName() + "(Id: " + ownAgentProb.extractID()
                        + ") is not assigned teamObserver enter the plan " + rp.getPlan().getName() + " and will IDLE!");
//#endif
                rp.setActiveState(null);
                rp.setOwnEntryPoint(null);
                return rp; // If we return here, this agent will idle (no ep at rp)
            }
            else
            {
                // assign found EntryPoint (this agent dont idle)
                rp.setOwnEntryPoint(ep);
            }

            // ACTIVE STATE set by RunningPlan
            if(oldRunningPlan == null)
            {
                // RECURSIVE PLANSELECTING FOR NEW STATE
                rpChildren = this.getPlansForStateInternal(rp, rp.getActiveState().getPlans(), rp.getAssignment().getAgentsWorking(ep));
            }
            else
            {
                if (CommonUtils.PS_DEBUG_debug) System.out.println( "PS: no recursion due teamObserver utilitycheck" );
                // Don't calculate children, because we have an
                // oldRp . we just replace the oldRp
                // (not its children . this will happen in an extra call)
                break;
            }
        } while (rpChildren == null);
        // WHEN WE GOT HERE, THIS AGENT WONT IDLE AND WE HAVE A
        // VALID ASSIGNMENT, WHICH PASSED ALL RUNTIME CONDITIONS
        if(rpChildren != null && rpChildren.size() != 0) // c# rpChildren != null
        {
            if (CommonUtils.PS_DEBUG_debug) System.out.println( "PS: Set child . father reference");
            rp.addChildren(rpChildren);
        }
        if (CommonUtils.PS_DEBUG_debug) System.out.println( "PS: Created RunningPlan: \n" + rp.toString() );
        ta = null;
        return rp; // If we return here, this agent is normal assigned
    }


    public void setPlanBase(PlanBase planBase) {
        this.planBase = planBase;
    }
}
