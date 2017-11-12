package de.uniks.vs.jalica.common;

import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.unknown.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by alex on 13.07.17.
 */
public class UtilityFunction {

    double DIFFERENCETHRESHOLD = 0.0001;

    private double priorityWeight;
    private double similarityWeight;
    private ArrayList<USummand> utilSummands;
    private UtilityInterval simUI;
    private UtilityInterval priResult;
    private IRoleAssignment ra;
    private LinkedHashMap<Long, Double> roleHighestPriorityMap;
    private TaskRoleStruct lookupStruct;
    private LinkedHashMap<TaskRoleStruct, Double> priorityMartix;
    private Plan plan;
    private AlicaEngine ae;

    public static void initDataStructures(AlicaEngine alicaEngine) {

    }

    public double eval(RunningPlan newRp, RunningPlan oldRp) {
        if (!newRp.getAssignment().isValid())
        {
            return -1.0;
        }
        UtilityInterval sumOfUI = new UtilityInterval(0.0, 0.0);
        double sumOfWeights = 0.0;

        // Sum up priority summand
        UtilityInterval prioUI = this.getPriorityResult(newRp.getAssignment());
        if (prioUI.getMax() < 0.0)
        {
            // one robot has a negative priority for his task . -1.0 for the complete assignment
            return -1;
        }
        sumOfUI.setMax(sumOfUI.getMax() + this.priorityWeight * prioUI.getMax());
        sumOfUI.setMin(sumOfUI.getMin() + this.priorityWeight * prioUI.getMin());
        sumOfWeights += this.priorityWeight;

        // Sum up all normal utility summands
        UtilityInterval curUI;
        for (int i = 0; i < this.utilSummands.size(); ++i)
        {
            USummand iter = utilSummands.get(i);
//            advance(iter, i);
            curUI = iter.eval(newRp.getAssignment());
            //if a summand deny assignment, return -1 for forbidden assignments
            if (curUI.getMax() == -1.0)
            {
                return -1.0;
            }
            sumOfWeights += iter.getWeight();
            sumOfUI.setMax(sumOfUI.getMax() + (iter).getWeight() * curUI.getMax());
            sumOfUI.setMin(sumOfUI.getMin() + (iter).getWeight() * curUI.getMin());
        }

        if (oldRp != null && this.similarityWeight > 0)
        {
            // Sum up similarity summand
            UtilityInterval simUI = this.getSimilarity(newRp.getAssignment(), oldRp.getAssignment());
            sumOfUI.setMax(sumOfUI.getMax() + this.similarityWeight * simUI.getMax());
            sumOfUI.setMin(sumOfUI.getMin() + this.similarityWeight * simUI.getMin());
            sumOfWeights += this.similarityWeight;
        }

        // Normalize to 0..1
        if (sumOfWeights > 0.0)
        {
            sumOfUI.setMax(sumOfUI.getMax() / sumOfWeights);
            sumOfUI.setMin(sumOfUI.getMin() / sumOfWeights);
            // Min == Max because RP.Assignment must be an complete Assignment!
            if ((sumOfUI.getMax() - sumOfUI.getMin()) > DIFFERENCETHRESHOLD)
            {
                System.err.println("UF: The utility min and max value differs more than " + DIFFERENCETHRESHOLD
                        + " for an Assignment!");
            }
            return sumOfUI.getMax();
        }

        return 0.0;
    }

    private UtilityInterval getSimilarity(IAssignment newAss, IAssignment oldAss) {
        simUI.setMax(0.0);
        simUI.setMin(0.0);
        // Calculate the similarity to the old Assignment
        int numOldAssignedRobots = 0;
        //shared_ptr<vector<EntryPoint*> > oldAssEps = oldAss.getEntryPoints();
        EntryPoint ep;
        for (short i = 0; i < oldAss.getEntryPointCount(); ++i)
        {
            ep = oldAss.getEpRobotsMapping().getEp(i);
            // for normalisation
            ArrayList<Integer> oldRobots = oldAss.getRobotsWorkingAndFinished(ep);
            numOldAssignedRobots += oldRobots.size();
            ArrayList<Integer> newRobots = newAss.getRobotsWorkingAndFinished(ep);

            //C# newRobots != null
            if (newRobots.size() != 0)
            {
                for (int oldRobot : oldRobots)
                {
                    if (CommonUtils.find(newRobots,0, newRobots.size()-1, oldRobot) != newRobots.get(newRobots.size()-1))
                    {
                        simUI.setMin(simUI.getMin() + 1);
                    }
                    else if (ep.getMaxCardinality() > newRobots.size()
                            && CommonUtils.find(newAss.getUnassignedRobots(), 0, newAss.getUnassignedRobots().size()-1,
                            oldRobot) != newAss.getUnassignedRobots().lastElement())
                    {
                        simUI.setMax(simUI.getMax() + 1);
                    }
                }
            }
        }

        simUI.setMax(simUI.getMax() + simUI.getMin());
        // Normalise if possible
        if (numOldAssignedRobots > 0)
        {
            simUI.setMin(simUI.getMin() / numOldAssignedRobots);
            simUI.setMax(simUI.getMax() / numOldAssignedRobots);

        }

        return simUI;
    }

    private UtilityInterval getPriorityResult(IAssignment ass) {
        this.priResult.setMax(0.0);
        this.priResult.setMin(0.0);
        if (this.priorityWeight == 0)
        {
            return this.priResult;
        }
        //c# != null
        // SUM UP HEURISTIC PART OF PRIORITY UTILITY

        if (ass.getUnassignedRobots().size() != 0) // == null, when it is a normal assignment
        {
            for (int robotID : ass.getUnassignedRobots())
            {

                this.priResult.setMax(
                    this.priResult.getMax()
                    + this.roleHighestPriorityMap.get(this.ra.getRole(robotID).getId()));
            }
        }
        // SUM UP DEFINED PART OF PRIORITY UTILITY

        // for better comparability of different utility functions
        int denum = Math.min(this.plan.getMaxCardinality(), this.ae.getTeamObserver().teamSize());
        long taskId;
        long roleId;
        //	shared_ptr<vector<EntryPoint*> > eps = ass.getEntryPoints();
        double curPrio = 0;
        EntryPoint ep;
        for (short i = 0; i < ass.getEntryPointCount(); ++i)
        {
            ep = ass.getEpRobotsMapping().getEp(i);
            taskId = ep.getTask().getId();
            ArrayList<Integer> robotList = ass.getUniqueRobotsWorkingAndFinished(ep);
            for(int robot : robotList)
            {
                roleId = this.ra.getRole(robot).getId();
                this.lookupStruct.taskId = taskId;
                this.lookupStruct.roleId = roleId;
                for (TaskRoleStruct key : this.priorityMartix.keySet())
                {
                    if (key.roleId == this.lookupStruct.roleId
                        && key.taskId == this.lookupStruct.taskId)
                    {
                        curPrio = this.priorityMartix.get(key);
                        break;
                    }
                }
                if (curPrio < 0.0) // because one Robot has a negative priority for his task
                {
                    this.priResult.setMin(-1.0);
                    this.priResult.setMax(-1.0);
                    return this.priResult;
                }
                this.priResult.setMin(this.priResult.getMin() + curPrio);
//#ifdef UFDEBUG
                double prio = 0;
                for(TaskRoleStruct key  : this.priorityMartix.keySet())
                {
                    if(key.roleId == this.lookupStruct.roleId && key.taskId == this.lookupStruct.taskId)
                    {
                        prio = this.priorityMartix.get(key);
                        break;
                    }
                }
                if (CommonUtils.UFDEBUG_debug) System.out.println("UF: taskId:" + taskId + " roleId:" + roleId + " prio: " + prio);
//#endif
            }
        }
//#ifdef UFDEBUG
        if (CommonUtils.UFDEBUG_debug)System.out.println( "##" );
        if (CommonUtils.UFDEBUG_debug)System.out.println( "UF: prioUI.Min = " + priResult.getMin());
        if (CommonUtils.UFDEBUG_debug)System.out.println( "UF: prioUI.Max = " + priResult.getMax() );
        if (CommonUtils.UFDEBUG_debug)System.out.println( "UF: denum = " + denum );
//#endif
        priResult.setMax(priResult.getMax() + priResult.getMin());
        if (denum != 0)
        {
            priResult.setMin(priResult.getMin() / denum);
            priResult.setMax(priResult.getMax() / denum);
        }
//#ifdef UFDEBUG
        if (CommonUtils.UFDEBUG_debug)System.out.println( "UF: prioUI.Min = " + priResult.getMin() );
        if (CommonUtils.UFDEBUG_debug)System.out.println( "UF: prioUI.Max = " + priResult.getMax() );
        if (CommonUtils.UFDEBUG_debug)System.out.println( "##" );
//#endif
        return priResult;
    }

    public void cacheEvalData() {
        if (this.utilSummands.size() != 0) // == null for default utility function
        {
            for (int i = 0; i < this.utilSummands.size(); ++i)
            {
                USummand iter = this.utilSummands.get(i);
//                advance(iter, i);
                (iter).cacheEvalData();
            }
        }
    }

    public void updateAssignment(IAssignment newAss, IAssignment oldAss) {
        UtilityInterval utilityInterval = this.eval(newAss, oldAss);
        newAss.setMin(utilityInterval.getMin());
        newAss.setMax(utilityInterval.getMax());
    }

    private UtilityInterval eval(IAssignment newAss, IAssignment oldAss) {
        UtilityInterval sumOfUI = new UtilityInterval(0.0, 0.0);
        double sumOfWeights = 0.0;

        // Sum up priority summand
        UtilityInterval prioUI = this.getPriorityResult(newAss);
        if (prioUI.getMax() == -1.0)
        {
            // one robot have a negativ priority for his task . (-1.0, -1.0) for the complete assignment
            return prioUI;
        }
        sumOfUI.setMax(sumOfUI.getMax() + this.priorityWeight * prioUI.getMax());
        sumOfUI.setMin(sumOfUI.getMin() + this.priorityWeight * prioUI.getMin());
        sumOfWeights += this.priorityWeight;
        // Sum up all normal utility summands
        UtilityInterval curUI;
        for (int i = 0; i < this.utilSummands.size(); ++i)
        {
            USummand iter = utilSummands.get(i);
//            advance(iter, i);
            curUI = (iter).eval(newAss);
            //if a summand deny assignment, return -1 for forbidden assignments
            if (curUI.getMax() == -1.0)
            {
                sumOfUI.setMax(-1.0);
                return sumOfUI;
            }
            sumOfWeights += (iter).getWeight();
            sumOfUI.setMax(sumOfUI.getMax() + (iter).getWeight() * curUI.getMax());
            sumOfUI.setMin(sumOfUI.getMin() + (iter).getWeight() * curUI.getMin());
        }
        if (oldAss != null && this.similarityWeight > 0)
        {
            // Sum up similarity summand
            UtilityInterval simUI = this.getSimilarity(newAss, oldAss);
            sumOfUI.setMax(sumOfUI.getMax() + this.similarityWeight * simUI.getMax());
            sumOfUI.setMin(sumOfUI.getMin() + this.similarityWeight * simUI.getMin());
            sumOfWeights += this.similarityWeight;
        }
        if (sumOfWeights > 0.0)
        {
            sumOfUI.setMax(sumOfUI.getMax() / sumOfWeights);
            sumOfUI.setMin(sumOfUI.getMin() / sumOfWeights);

            return sumOfUI;
        }

        sumOfUI.setMin(0.0);
        sumOfUI.setMax(0.0);
        return sumOfUI;
    }
}
