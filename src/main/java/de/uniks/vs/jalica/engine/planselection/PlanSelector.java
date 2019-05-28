package de.uniks.vs.jalica.engine.planselection;

import de.uniks.vs.jalica.engine.Assignment;
import de.uniks.vs.jalica.engine.RunningPlan;
import de.uniks.vs.jalica.engine.model.*;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.ITeamObserver;
import de.uniks.vs.jalica.engine.collections.AgentProperties;
import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.common.PlanningProblem;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by alex on 13.07.17.
 */
public class PlanSelector implements IPlanSelector {
    private PartialAssignmentPool partialAssignmentPool;
    private AlicaEngine alicaEngine;
    private ITeamObserver teamObserver;

    public PlanSelector(AlicaEngine alicaEngine, PartialAssignmentPool assignmentPool) {
        this.alicaEngine = alicaEngine;
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
        // GET ROBOTS TO ASSIGN
        Vector<Long> selectedAgents = runningPlan.getAssignment().getAllAgents();
        return this.createRunningPlan(runningPlan.getParent(), newPlanList, selectedAgents, runningPlan, runningPlan.getPlanType());
    }

    private ArrayList<RunningPlan> getPlansForStateInternal(RunningPlan planningParent, ArrayList<AbstractPlan> abstractPlans, Vector<Long> agentIDs) {
        ArrayList<RunningPlan> rps = new ArrayList<RunningPlan>();
        if (CommonUtils.PS_DEBUG_debug) System.out.println("###### PS: GetPlansForState: Parent:"
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
//            bc = (BehaviourConfiguration) ap;

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
//                p = (Plan)(ap);
//                if (p != null)

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
                                          Vector<Long> agentIDs, RunningPlan oldRp, PlanType relevantPlanType) {
        ArrayList<Plan> newPlanList = new ArrayList<>();
        // REMOVE EVERY PLAN WITH TOO GREAT MIN CARDINALITY
        for (Plan plan : plans)
        {
            // CHECK: number of agents < minimum cardinality of this plan
            if (plan.getMinCardinality() > (agentIDs.size() + teamObserver.successesInPlan(plan)))
            {

                String ss = "";
                ss += "PS: AgentIDs: ";
                for (long agent : agentIDs)
                {
                    ss += agent + ", ";
                }
                ss += "= " + agentIDs.size() + " IDs are not enough for the plan " + plan.getName() + "!" ;
                //this.baseModule.Mon.Error(1000, msg);

                if (CommonUtils.PS_DEBUG_debug) System.out.println(ss);
            }
            else
            {
                // this plan was ok according teamObserver its cardinalities, so we can add it
                newPlanList.add(plan);
            }
        }

        // WE HAVE NOT ENOUGH AGENTS TO EXECUTE ANY PLAN
        if (newPlanList.size() == 0)
        {
            return null;
        }
        // TASKASSIGNMENT
        TaskAssignment ta = null;
        Assignment oldAss = null;
        RunningPlan rp;
        if (oldRp == null)
        {
            // preassign other agents, because we dont need a similar assignment
            rp = new RunningPlan(alicaEngine, relevantPlanType);
            ta = new TaskAssignment(this.alicaEngine.getPartialAssignmentPool(), this.alicaEngine.getTeamObserver(), newPlanList, agentIDs, true);
        }
        else
        {
            // dont preassign other agents, because we need a similar assignment (not the same)
            rp = new RunningPlan(alicaEngine, oldRp.getPlanType());
            ta = new TaskAssignment(this.alicaEngine.getPartialAssignmentPool(), this.alicaEngine.getTeamObserver(), newPlanList, agentIDs, false);
            oldAss = oldRp.getAssignment();
        }

        // some variables for the do while loop
        EntryPoint ep = null;
        AgentProperties ownAgentProb = teamObserver.getOwnAgentProperties();
        // PLANNINGPARENT
        rp.setParent(planningParent);
        ArrayList<RunningPlan> rpChildren = null;

        do
        {
            // ASSIGNMENT
            rp.setAssignment(ta.getNextBestAssignment(oldAss));

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
            if (CommonUtils.PS_DEBUG_debug) System.out.println( "PS: rp.Assignment of Plan " + rp.getPlan().getName() + " from " + ownAgentProb.getID() + " is: " + rp.getAssignment().toString());
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
            ep = rp.getAssignment().getEntryPointOfAgent(ownAgentProb.getID());

            if (ep == null)
            {
//#ifdef PSDEBUG
                if (CommonUtils.PS_DEBUG_debug) System.out.println( "PS: The agent " + ownAgentProb.getName() + "(Id: " + ownAgentProb.getID()
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
            if(oldRp == null)
            {
                // RECURSIVE PLANSELECTING FOR NEW STATE
                rpChildren = this.getPlansForStateInternal(rp, rp.getActiveState().getPlans(), rp.getAssignment().getAgentsWorking(ep));
            }
            else
            {
//#ifdef PSDEBUG
                if (CommonUtils.PS_DEBUG_debug) System.out.println( "PS: no recursion due teamObserver utilitycheck" );
//#endif
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
//#ifdef PSDEBUG
            if (CommonUtils.PS_DEBUG_debug) System.out.println( "PS: Set child . father reference");
//#endif
            rp.addChildren(rpChildren);
        }
//#ifdef PSDEBUG
        if (CommonUtils.PS_DEBUG_debug) System.out.println( "PS: Created RunningPlan: \n" + rp.toString() );
//#endif
        ta = null;
        return rp; // If we return here, this agent is normal assigned
    }
        

}
