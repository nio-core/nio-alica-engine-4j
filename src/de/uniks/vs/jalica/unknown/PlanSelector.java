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
    private PartialAssignmentPool pap;
    private AlicaEngine ae;
    private ITeamObserver to;

    public PlanSelector(AlicaEngine alicaEngine, PartialAssignmentPool pap) {
        this.ae = alicaEngine;
        this.pap = pap;
    }

    @Override
    public ArrayList<RunningPlan> getPlansForState(RunningPlan planningParent,
                                                   ArrayList<AbstractPlan> plans, Vector<Integer> robotIDs) {
        PartialAssignment.reset(pap);
        ArrayList<RunningPlan> ll = this.getPlansForStateInternal(planningParent, plans, robotIDs);
        return ll;
    }

    @Override
    public RunningPlan getBestSimilarAssignment(RunningPlan rp, Vector<Integer> robots) {
        CommonUtils.aboutNoImpl();
        return null;
    }

    private ArrayList<RunningPlan> getPlansForStateInternal(RunningPlan planningParent, ArrayList<AbstractPlan> plans, Vector<Integer> robotIDs) {
        ArrayList<RunningPlan> rps = new ArrayList<RunningPlan>();
//#ifdef PSDEBUG
        System.out.println("<######PS: GetPlansForState: Parent:"
                + (planningParent != null ? planningParent.getPlan().getName() : "null") + " plan count: "
                + plans.size() + " robot count: "
                + robotIDs.size() + " ######>" );
//#endif
        RunningPlan rp;
        ArrayList<Plan> planList;
        BehaviourConfiguration bc;
        Plan p;
        PlanType pt;
        PlanningProblem pp;
        for (AbstractPlan ap : plans)
        {
            // BEHAVIOUR CONFIGURATION
            bc = (BehaviourConfiguration)(ap);
            if (bc != null)
            {
                rp = new RunningPlan(ae, bc);
                // A BehaviourConfiguration is a Plan too (in this context)
                rp.setPlan(bc);
                rps.add(rp);
                rp.setParent(planningParent);
//#ifdef PSDEBUG
                System.out.println("PS: Added Behaviour " + bc.getBehaviour().getName() );
//#endif
            }
            else
            {
                // PLAN
//                p = (Plan)(ap);
//                if (p != null)
                if ((ap instanceof Plan))
                {
                    p = (Plan)ap;
                    planList = new ArrayList<Plan>();
                    planList.add(p);
                    rp = this.createRunningPlan(planningParent, planList, robotIDs, null, null);
                    if (rp == null)
                    {
//#ifdef PSDEBUG
                        System.out.println("PS: It was not possible to create a RunningPlan for the Plan " + p.getName() + "!"
                        );
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
                        rp = this.createRunningPlan(planningParent, pt.getPlans(), robotIDs, null, pt);
                        if (rp == null)
                        {
//#ifdef PSDEBUG
                            System.out.println( "PS: It was not possible to create a RunningPlan for the Plan " + pt.getName()
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
                            System.err.println( "PS: WTF? An AbstractPlan wasnt a BehaviourConfiguration, a Plan, a PlanType nor a PlannigProblem: "
                                    + ap.getId() );
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
                        rp = this.createRunningPlan(planningParent, planList, robotIDs, null, null);
                        if (rp == null)
                        {
//#ifdef PSDEBUG
                            System.out.println( "PS: Unable to execute planning result" );
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
                                          Vector<Integer> robotIDs, RunningPlan oldRp, PlanType relevantPlanType) {
        ArrayList<Plan> newPlanList = new ArrayList<>();
        // REMOVE EVERY PLAN WITH TOO GREAT MIN CARDINALITY
        for (Plan plan : plans)
        {
            // CHECK: number of robots < minimum cardinality of this plan
            if (plan.getMinCardinality() > (robotIDs.size()
                    + to.successesInPlan(plan)))
            {
//#ifdef PSDEBUG
                String ss = "";
                ss += "PS: RobotIds: ";
                for (int robot : robotIDs)
                {
                    ss += robot + ", ";
                }
                ss += "= " + robotIDs.size() + " IDs are not enough for the plan " + plan.getName() + "!" ;
                //this.baseModule.Mon.Error(1000, msg);

                System.out.println(ss);
//#endif
            }
            else
            {
                // this plan was ok according to its cardinalities, so we can add it
                newPlanList.add(plan);
            }
        }
        // WE HAVE NOT ENOUGH ROBOTS TO EXECUTE ANY PLAN
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
            // preassign other robots, because we dont need a similar assignment
            rp = new RunningPlan(ae, relevantPlanType);
            ta = new TaskAssignment(this.ae.getPartialAssignmentPool(), this.ae.getTeamObserver(), newPlanList, robotIDs, true);
        }
        else
        {
            // dont preassign other robots, because we need a similar assignment (not the same)
            rp = new RunningPlan(ae, oldRp.getPlanType());
            ta = new TaskAssignment(this.ae.getPartialAssignmentPool(), this.ae.getTeamObserver(), newPlanList, robotIDs, false);
            oldAss = oldRp.getAssignment();
        }


        // some variables for the do while loop
        EntryPoint ep = null;
        RobotProperties ownRobProb = to.getOwnRobotProperties();
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
                System.out.println( "PS: rp.Assignment is NULL" );
//#endif
                return null;
            }

            // PLAN (needed for Conditionchecks)
            rp.setPlan(rp.getAssignment().getPlan());
//#ifdef PSDEBUG
            System.out.println( "PS: rp.Assignment of Plan " + rp.getPlan().getName() + " from " + ownRobProb.getId() + " is: " + rp.getAssignment().toString());
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
            ep = rp.getAssignment().getEntryPointOfRobot(ownRobProb.getId());

            if (ep == null)
            {
//#ifdef PSDEBUG
                System.out.println( "PS: The robot " + ownRobProb.getName() + "(Id: " + ownRobProb.getId()
                        + ") is not assigned to enter the plan " + rp.getPlan().getName() + " and will IDLE!");
//#endif
                rp.setActiveState(null);
                rp.setOwnEntryPoint(null);
                return rp; // If we return here, this robot will idle (no ep at rp)
            }
            else
            {
                // assign found EntryPoint (this robot dont idle)
                rp.setOwnEntryPoint(ep);
            }
            // ACTIVE STATE set by RunningPlan
            if(oldRp == null)
            {
                // RECURSIVE PLANSELECTING FOR NEW STATE
                rpChildren = this.getPlansForStateInternal(rp, rp.getActiveState().getPlans(), rp.getAssignment().getRobotsWorking(ep));
            }
            else
            {
//#ifdef PSDEBUG
                System.out.println( "PS: no recursion due to utilitycheck" );
//#endif
                // Don't calculate children, because we have an
                // oldRp . we just replace the oldRp
                // (not its children . this will happen in an extra call)
                break;
            }
        } while (rpChildren == null);
        // WHEN WE GOT HERE, THIS ROBOT WONT IDLE AND WE HAVE A
        // VALID ASSIGNMENT, WHICH PASSED ALL RUNTIME CONDITIONS
        if(rpChildren != null && rpChildren.size() != 0) // c# rpChildren != null
        {
//#ifdef PSDEBUG
            System.out.println( "PS: Set child . father reference");
//#endif
            rp.addChildren(rpChildren);
        }
//#ifdef PSDEBUG
        System.out.println( "PS: Created RunningPlan: \n" + rp.toString() );
//#endif
        ta = null;
        return rp; // If we return here, this robot is normal assigned
    }
        

}
