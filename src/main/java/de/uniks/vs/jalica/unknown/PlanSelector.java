package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.behaviours.BehaviourConfiguration;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.teamobserver.ITeamObserver;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by alex on 13.07.17.
 */
public class PlanSelector implements IPlanSelector {
    private PartialAssignmentPool partialAssignmentPool;
    private AlicaEngine ae;
    private ITeamObserver teamObserver;

    public PlanSelector(AlicaEngine alicaEngine, PartialAssignmentPool pap) {
        this.ae = alicaEngine;
        this.teamObserver = ae.getTeamObserver();
        this.partialAssignmentPool = pap;
    }

    @Override
    public ArrayList<RunningPlan> getPlansForState(RunningPlan planningParent,
                                                   ArrayList<AbstractPlan> plans, Vector<Long> agents) {
        PartialAssignment.reset(partialAssignmentPool);
        ArrayList<RunningPlan> newPlans = this.getPlansForStateInternal(planningParent, plans, agents);
        return newPlans;
    }

    @Override
    public RunningPlan getBestSimilarAssignment(RunningPlan rp, Vector<Long> agents) {
        // Reset set index of the partial assignment multiton
        PartialAssignment.reset(partialAssignmentPool);
        // CREATE NEW PLAN LIST
        ArrayList<Plan> newPlanList;

        if (rp.getPlanType() == null) {
            newPlanList = new ArrayList<Plan>();
            newPlanList.add((Plan) rp.getPlan());
        }
        else
        {
            newPlanList = rp.getPlanType().getPlans();
        }
        // GET ROBOTS TO ASSIGN
        Vector<Long> selectedAgents = rp.getAssignment().getAllAgents();
        return this.createRunningPlan(rp.getParent(), newPlanList, selectedAgents, rp, rp.getPlanType());
    }

    private ArrayList<RunningPlan> getPlansForStateInternal(RunningPlan planningParent, ArrayList<AbstractPlan> plans, Vector<Long> agentIDs) {
        ArrayList<RunningPlan> rps = new ArrayList<RunningPlan>();
//#ifdef PSDEBUG
        if (CommonUtils.PS_DEBUG_debug) System.out.println("###### PS: GetPlansForState: Parent:"
                + (planningParent != null ? planningParent.getPlan().getName() : "null") + " plan count: "
                + plans.size() + " agent count: "
                + agentIDs.size() + " ######" );
//#endif
        RunningPlan rp;
        ArrayList<Plan> planList;
        BehaviourConfiguration bc;
        Plan p;
        PlanType pt;
        PlanningProblem pp;

        for (AbstractPlan ap : plans) {
            // BEHAVIOUR CONFIGURATION
//            bc = (BehaviourConfiguration) ap;

            if (ap == null) {
                System.out.println("PS: plan is null");
                continue;
            }

            if (ap instanceof BehaviourConfiguration) {
                bc = (BehaviourConfiguration) ap;
                rp = new RunningPlan(ae, bc);
                // A BehaviourConfiguration is a Plan too (in this context)
                rp.setPlan(bc);
                rps.add(rp);
                rp.setParent(planningParent);
//#ifdef PSDEBUG
                if (CommonUtils.PS_DEBUG_debug) System.out.println("PS: Added Behaviour " + bc.getBehaviour().getName() );
//#endif
            } else
            {
                // PLAN
//                p = (Plan)(ap);
//                if (p != null)
                if ((ap instanceof Plan))
                {
                    p = (Plan)ap;
                    planList = new ArrayList<>();
                    planList.add(p);
                    rp = this.createRunningPlan(planningParent, planList, agentIDs, null, null);
                    if (rp == null)
                    {
//#ifdef PSDEBUG
                        if (CommonUtils.PS_DEBUG_debug) System.out.println("PS: It was not possible teamObserver create a RunningPlan for the Plan " + p.getName() + "!");
//#endif
                        return null;
                    }
                    rps.add(rp);
                }
                else
                {
                    // PLANTYPE
//                    pt = dynamic_cast<PlanType*>(ap);
//                    if (pt != null)
                    if (ap instanceof PlanType)
                    {
                        pt = (PlanType)(ap);
                        rp = this.createRunningPlan(planningParent, pt.getPlans(), agentIDs, null, pt);
                        if (rp == null)
                        {
//#ifdef PSDEBUG
                            if (CommonUtils.PS_DEBUG_debug) System.out.println( "PS: It was not possible teamObserver create a RunningPlan for the Plan " + pt.getName()
                                    + "!" );
//#endif
                            return null;
                        }
                        rps.add(rp);
                    }
                    else
                    {
                        pp = null;
//                        pp = dynamic_cast<PlanningProblem*>(ap);
//                        if (pp == null)
                        if (!(ap instanceof PlanningProblem))
                        {
                            System.err.println( "PS: WTF? An AbstractPlan wasnt a BehaviourConfiguration, a Plan, a PlanType nor a PlannigProblem: " + ap.getID() );
                            try {
                                throw new Exception();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else
                            pp = (PlanningProblem)ap;

                        //TODO implement method in planner
                        Plan myP = ae.getPlanner().requestPlan(pp);
                        planList = new ArrayList<Plan>();
                        planList.add(myP);
                        rp = this.createRunningPlan(planningParent, planList, agentIDs, null, null);
                        if (rp == null)
                        {
//#ifdef PSDEBUG
                            if (CommonUtils.PS_DEBUG_debug) System.out.println( "PS: Unable teamObserver execute planning result" );
//#endif
                            return null;
                        }
                        rps.add(rp);
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
//#ifdef PSDEBUG
                String ss = "";
                ss += "PS: AgentIDs: ";
                for (long agent : agentIDs)
                {
                    ss += agent + ", ";
                }
                ss += "= " + agentIDs.size() + " IDs are not enough for the plan " + plan.getName() + "!" ;
                //this.baseModule.Mon.Error(1000, msg);

                if (CommonUtils.PS_DEBUG_debug) System.out.println(ss);
//#endif
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
            rp = new RunningPlan(ae, relevantPlanType);
            ta = new TaskAssignment(this.ae.getPartialAssignmentPool(), this.ae.getTeamObserver(), newPlanList, agentIDs, true);
        }
        else
        {
            // dont preassign other agents, because we need a similar assignment (not the same)
            rp = new RunningPlan(ae, oldRp.getPlanType());
            ta = new TaskAssignment(this.ae.getPartialAssignmentPool(), this.ae.getTeamObserver(), newPlanList, agentIDs, false);
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
