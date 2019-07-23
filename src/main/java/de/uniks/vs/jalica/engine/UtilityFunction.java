package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.engine.idmanagement.ID;
import de.uniks.vs.jalica.engine.model.*;
import de.uniks.vs.jalica.engine.planselection.IAssignment;
import de.uniks.vs.jalica.engine.planselection.PartialAssignment;
import de.uniks.vs.jalica.engine.planselection.views.PartialAssignmentSuccessView;
import de.uniks.vs.jalica.engine.planselection.views.PartialAssignmentView;
import de.uniks.vs.jalica.engine.taskassignment.TaskRole;
import de.uniks.vs.jalica.engine.teammanagement.view.AssignmentSuccessView;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by alex on 13.07.17.
 * Updated 23.6.19
 */
public class UtilityFunction {

    private static final double DIFFERENCE_THRESHOLD = 0.0001;
    private Plan plan;
    private ArrayList<USummand> utilSummands;
    private LinkedHashMap<TaskRole, Double> priorityMartix;
    private LinkedHashMap<Long, Double> roleHighestPriorityMap;  // Role, Priority
    private double priorityWeight;
    private double similarityWeight;
    private AlicaEngine ae;
    private IRoleAssignment ra;

    public UtilityFunction(double priorityWeight, double similarityWeight, Plan plan) {
        this.plan = plan;
        this.ra = null;
        this.ae = null;
        this.priorityWeight = priorityWeight;
        this.similarityWeight = similarityWeight;
        this.utilSummands = new ArrayList<>();
        this.priorityMartix = new LinkedHashMap<>();
        this.roleHighestPriorityMap = new LinkedHashMap<>();
    }

    public UtilityInterval eval(PartialAssignment newAss, Assignment oldAss) {
        if (!newAss.isValid()) {
            return new UtilityInterval(-1.0, -1.0);
        }
        UtilityInterval sumOfUI = new UtilityInterval(0.0, 0.0);
        double sumOfWeights = 0.0;

        IAssignment wrapper = new IAssignment(newAss);
        // Sum up priority summand
        UtilityInterval prioUI = getPriorityResult(wrapper);
        if (prioUI.getMax() <= -1.0) {
            // one agent have a negativ priority for his task -> (-1.0, -1.0) for the complete assignment
            return prioUI;
        }
//        sumOfUI += this.priorityWeight * prioUI;
        sumOfUI.sumWith(this.priorityWeight, prioUI);

        sumOfWeights += this.priorityWeight;
        // Sum up all normal utility summands
        UtilityInterval curUI;

        for (USummand us : this.utilSummands) {
            curUI = us.eval(wrapper);
            // if a summand deny assignment, return -1 for forbidden assignments
            if (curUI.getMax() <= -1.0) {
                sumOfUI.setMax(-1.0);
                return sumOfUI;
            }
            sumOfWeights += us.getWeight();
//        sumOfUI += us.getWeight() * curUI;
            sumOfUI.sumWith(us.weight, curUI);
        }
        if (oldAss != null && this.similarityWeight > 0) {
            // Sum up similarity summand
            UtilityInterval simUI = getSimilarity(wrapper, oldAss);
//            sumOfUI += this.similarityWeight * simUI;
            sumOfUI.sumWith(this.similarityWeight, simUI);
            sumOfWeights += this.similarityWeight;
        }
        if (sumOfWeights > 0.0) {
//            sumOfUI /= sumOfWeights;
            sumOfUI.devideBy(sumOfWeights);
            return sumOfUI;
        }
        return new UtilityInterval(0.0, 0.0);
    }

    public void cacheEvalData() {
        for (USummand us : this.utilSummands) {
            us.cacheEvalData();
        }
    }

    void init(AlicaEngine ae) {
        // CREATE MATRIX && HIGHEST PRIORITY ARRAY
        // init dicts
        this.roleHighestPriorityMap.clear();
        this.priorityMartix.clear();
        RoleSet roleSet = ae.getRoleSet();

        for (Role role : roleSet.getRoles()) {
            long roleId = role.getID();
            long taskId;
            this.roleHighestPriorityMap.put(roleId, 0.0);
            for (EntryPoint ep : this.plan.getEntryPoints()) {
                taskId = ep.getTask().getID();
                double curPrio = role.getPriority(taskId);
                this.priorityMartix.put(new TaskRole(taskId, roleId), curPrio);

                if (this.roleHighestPriorityMap.get(roleId) < curPrio) {
                    this.roleHighestPriorityMap.replace(roleId, curPrio);
                }
            }
            // Add Priority for Idle-EntryPoint
            this.priorityMartix.put(new TaskRole(Task.IDLEID, roleId), 0.0);
        }
        this.ae = ae;
        this.ra = this.ae.getRoleAssignment();
    }

    static void initDataStructures(AlicaEngine ae) {
        for (Plan p : ae.getPlanRepository().getPlans().values()) {
            p.getUtilityFunction().init(ae);
        }
    }

    UtilityInterval getPriorityResult(IAssignment ass) {
        UtilityInterval priResult = new UtilityInterval(0.0, 0.0);
        if (this.priorityWeight == 0) {
            return priResult;
        }
        // SUM UP HEURISTIC PART OF PRIORITY UTILITY

        PartialAssignmentView unassignedAgents = ass.getUnassignedAgents();
        System.out.println(unassignedAgents);

        for (ID agentID : ass.getUnassignedAgents()) {
            Double highestPriority = this.roleHighestPriorityMap.get(this.ra.getRole(agentID).getID());
            assert (highestPriority != null);
            priResult.setMax(priResult.getMax() + highestPriority);
        }
        // SUM UP DEFINED PART OF PRIORITY UTILITY

        for (int i = 0; i < ass.getEntryPointCount(); ++i) {
            EntryPoint ep = ass.getEntryPoint(i);
            long taskId = ep.getTask().getID();

            for (ID agent : ass.getUniqueAgentsWorkingAndFinished(ep)) {
                double curPrio = 0;
                long roleId = this.ra.getRole(agent).getID();

                for (TaskRole taskRole : this.priorityMartix.keySet()) {
                    if (taskRole.taskId == taskId && taskRole.roleId == roleId) {
                        curPrio = this.priorityMartix.get(taskRole);
                    }
                }
                if (curPrio < 0.0) // because one Robot has a negative priority for his task
                {
                    return new UtilityInterval(-1.0, -1.0);
                }
                priResult.setMin(priResult.getMin() + curPrio);

                System.out.println("UF: taskId:" + taskId + " roleId:" + roleId + " prio: " + curPrio);
            }
        }
        // for better comparability of different utility functions
        int denum = Math.min(this.plan.getMaxCardinality(), this.ae.getTeamManager().getTeamSize());

        System.out.println("##" + "\n" + "UF: prioUI = " + priResult);
        System.out.println("UF: denum = " + denum);

        priResult.setMax(priResult.getMax() + priResult.getMin());
        if (denum != 0) {
//            priResult /= denum;
            priResult.devideBy(denum);
        }

        System.out.println("UF: prioUI = " + priResult);
        System.out.println("##");
        return priResult;
    }

    UtilityInterval getSimilarity(IAssignment newAss, Assignment oldAss) {
        UtilityInterval simUI = new UtilityInterval(0.0, 0.0);
        // Calculate the similarity to the old Assignment
        int numOldAssignedRobots = 0;
        // shared_ptr<vector<EntryPoint*> > oldAssEps = oldAss->getEntryPoints();
        for (short i = 0; i < oldAss.getEntryPointCount(); ++i) {
            EntryPoint ep = oldAss.getEntryPoint(i);
            AssignmentSuccessView oldRobots = oldAss.getAgentsWorkingAndFinished(ep);
            PartialAssignmentSuccessView newRobots = newAss.getAgentsWorkingAndFinished(ep);
            // for normalisation
            numOldAssignedRobots += oldRobots.size();

            if (!newRobots.isEmpty()) {

                for (ID oldRobot : oldRobots.get()) {

                    if (newRobots.contains(oldRobot)) {
                        simUI.setMin(simUI.getMin() + 1);
                    } else if (ep.getMaxCardinality() > newRobots.size() && newAss.getUnassignedAgents().contains(oldRobot)) {
                        simUI.setMax(simUI.getMax() + 1);
                    }
                }
            }
        }

        simUI.setMax(simUI.getMax() + simUI.getMin());
        // Normalise if possible
        if (numOldAssignedRobots > 0) {
//            simUI /= numOldAssignedRobots;
            simUI.devideBy(numOldAssignedRobots);
        }

        return simUI;
    }

    @Override
    public String toString() {
        String ss = "UtilityFunction: prioW: " + " simW: " + this.similarityWeight + "\n";

        for (USummand utilSummand : this.utilSummands) {
            ss += utilSummand.toString();
        }
        return ss;
    }

    public ArrayList<USummand> getUtilSummands() {
        return this.utilSummands;
    }

    ;

    Plan getPlan() {
        return this.plan;
    }


//    public static void initDataStructures(AlicaEngine ae) {
//        HashMap<Long, Plan> plans = ae.getPlanRepository().getPlans();
//
//        for (Plan plan : plans.values()) {
//            plan.getUtilityFunction().init(ae);
//        }
//    }
//
//    private void init(AlicaEngine ae) {
//        // CREATE MATRIX && HIGHEST PRIORITY ARRAY
//        // init dicts
//        this.roleHighestPriorityMap = new LinkedHashMap<Long, Double>();
//        this.priorityMartix = new LinkedHashMap<TaskRole, Double> ();
//        RoleSet roleSet = ae.getRoleSet();
//        long taskId;
//        long roleId;
//        Double curPriority;
//
//        for (RoleTaskMapping roleTaskMapping : roleSet.getRoleTaskMappings()) {
//            roleId = roleTaskMapping.getRole().getID();
//            this.roleHighestPriorityMap.put(roleId, 0.0);
//
//            for (EntryPoint entryPoint : this.plan.getEntryPoints()) {
//                taskId = entryPoint.getTask().getID();
//                curPriority = roleTaskMapping.getTaskPriorities().get(taskId);
//
//                if (curPriority == null) {
//                    curPriority = roleSet.getDefaultPriority();
////                    System.out.println("UF: There is no priority for the task " + taskId + " in the roleTaskMapping of the role "
////                            + roleTaskMapping.getRole().getName() + " with id " + roleId
////                        + "!\n We are in the UF for the plan " + this.plan.getName() + "!" );
//                }
//                TaskRole taskRole = new TaskRole(taskId, roleId);
//
//                if (this.priorityMartix.get(taskRole) == null) {
//                    this.priorityMartix.put(taskRole, curPriority);
//                }
//
//                if (this.roleHighestPriorityMap.get(roleId) < curPriority) {
//                    this.roleHighestPriorityMap.put(roleId, curPriority);
//                }
//            }
//            // Add Priority for Idle-EntryPoint
//            this.priorityMartix.put(new TaskRole(Task.IDLEID, roleId), 0.0);
//        }
//        //c# != null
//        // INIT UTILITYSUMMANDS
//        if (this.utilSummands.size() != 0) {
//            // it is null for default utility function
//
//            for (USummand utilSum : this.utilSummands){
//                utilSum.init(ae);
//            }
//        }
//        this.ae = ae;
//        this.ra = this.ae.getRoleAssignment();
//    }
//
//    public UtilityInterval eval(PartialAssignment newAss, Assignment oldAss) {
//
//        if (!newAss.isValid()) {
//            return new UtilityInterval(-1.0, -1.0);
//        }
//        UtilityInterval sumOfUI = new UtilityInterval(0.0, 0.0);
//        double sumOfWeights = 0.0;
//
////        IAssignment wrapper{newAss};
//        // Sum up priority summand
//        UtilityInterval prioUI = getPriorityResult(newAss);
//        if (prioUI.getMax() <= -1.0) {
//            // one robot have a negativ priority for his task -> (-1.0, -1.0) for the complete assignment
//            return prioUI;
//        }
//
////        sumOfUI += this.priorityWeight * prioUI;
//        sumOfUI.setMax(sumOfUI.getMax() + this.priorityWeight * prioUI.getMax());
//        sumOfUI.setMin(sumOfUI.getMin() + this.priorityWeight * prioUI.getMin());
//        sumOfWeights += this.priorityWeight;
//
//        // Sum up all normal utility summands
//        UtilityInterval curUI;
//
//        for (USummand us : this.utilSummands) {
//            curUI = us.eval(newAss);
//            // if a summand deny assignment, return -1 for forbidden assignments
//            if (curUI.getMax() <= -1.0) {
//                sumOfUI.setMax(-1.0);
//                return sumOfUI;
//            }
//            sumOfWeights += us.getWeight();
////            sumOfUI += us.getWeight() * curUI;
//            sumOfUI.setMax(sumOfUI.getMax() + us.getWeight() * curUI.getMax());
//            sumOfUI.setMin(sumOfUI.getMin() + us.getWeight() * curUI.getMin());
//        }
//        if (oldAss != null && this.similarityWeight > 0) {
//            // Sum up similarity summand
//            UtilityInterval simUI = getSimilarity(newAss, oldAss);
////            sumOfUI += this.similarityWeight * simUI;
//            sumOfUI.setMax(sumOfUI.getMax() + this.similarityWeight * simUI.getMax());
//            sumOfUI.setMin(sumOfUI.getMin() + this.similarityWeight * simUI.getMin());
//            sumOfWeights += this.similarityWeight;
//        }
//        if (sumOfWeights > 0.0) {
////            sumOfUI /= sumOfWeights;
//            sumOfUI.setMax(sumOfUI.getMax() / sumOfWeights);
//            sumOfUI.setMin(sumOfUI.getMin() / sumOfWeights);
//            return sumOfUI;
//        }
//        return new UtilityInterval(0.0, 0.0);
//    }
//
//
//    @Deprecated
//    public double eval(RunningPlan newRp, RunningPlan oldRp) {
//        if (!newRp.getAssignment().isValid())
//        {
//            return -1.0;
//        }
//        UtilityInterval sumOfUI = new UtilityInterval(0.0, 0.0);
//        double sumOfWeights = 0.0;
//
//        // Sum up priority summand
//        UtilityInterval prioUI = this.getPriorityResult(newRp.getAssignment());
//        if (prioUI.getMax() < 0.0)
//        {
//            // one robot has a negative priority for his task . -1.0 for the complete assignment
//            return -1;
//        }
//        sumOfUI.setMax(sumOfUI.getMax() + this.priorityWeight * prioUI.getMax());
//        sumOfUI.setMin(sumOfUI.getMin() + this.priorityWeight * prioUI.getMin());
//        sumOfWeights += this.priorityWeight;
//
//        // Sum up all normal utility summands
//        UtilityInterval curUI;
//        for (int i = 0; i < this.utilSummands.size(); i++)
//        {
//            USummand iter = utilSummands.get(i);
////            advance(iter, i);
//            curUI = iter.eval(newRp.getAssignment());
//            //if a summand deny assignment, return -1 for forbidden assignments
//            if (curUI.getMax() == -1.0)
//            {
//                return -1.0;
//            }
//            sumOfWeights += iter.getWeight();
//            sumOfUI.setMax(sumOfUI.getMax() + (iter).getWeight() * curUI.getMax());
//            sumOfUI.setMin(sumOfUI.getMin() + (iter).getWeight() * curUI.getMin());
//        }
//
//        if (oldRp != null && this.similarityWeight > 0)
//        {
//            // Sum up similarity summand
//            UtilityInterval simUI = this.getSimilarity(newRp.getAssignment(), oldRp.getAssignment());
//            sumOfUI.setMax(sumOfUI.getMax() + this.similarityWeight * simUI.getMax());
//            sumOfUI.setMin(sumOfUI.getMin() + this.similarityWeight * simUI.getMin());
//            sumOfWeights += this.similarityWeight;
//        }
//
//        // Normalize teamObserver 0..1
//        if (sumOfWeights > 0.0)
//        {
//            sumOfUI.setMax(sumOfUI.getMax() / sumOfWeights);
//            sumOfUI.setMin(sumOfUI.getMin() / sumOfWeights);
//            // Min == Max because RP.Assignment must be an complete Assignment!
//            if ((sumOfUI.getMax() - sumOfUI.getMin()) > DIFFERENCE_THRESHOLD)
//            {
//                System.err.println("UF: The utility min and max value differs more than " + DIFFERENCE_THRESHOLD
//                        + " for an Assignment!");
//            }
//            return sumOfUI.getMax();
//        }
//
//        return 0.0;
//    }
//
//    protected UtilityInterval getSimilarity(IAssignment newAss, IAssignment oldAss) {
//        simUI.setMax(0.0);
//        simUI.setMin(0.0);
//        // Calculate the similarity teamObserver the old Assignment
//        int numOldAssignedAgents = 0;
//        //shared_ptr<vector<EntryPoint*> > oldAssEps = oldAss.getEntryPoints();
//        EntryPoint ep;
//        for (short i = 0; i < oldAss.getEntryPointCount(); ++i)
//        {
//            ep = oldAss.getEpAgentsMapping().getEntryPoint(i);
//            // for normalisation
//            ArrayList<Long> oldAgents = oldAss.getAgentsWorkingAndFinished(ep);
//            numOldAssignedAgents += oldAgents.size();
//            ArrayList<Long> newAgents = newAss.getAgentsWorkingAndFinished(ep);
//
//            //C# newAgents != null
//            if (newAgents.size() != 0)
//            {
//                for (long oldAgent : oldAgents)
//                {
//                    if (CommonUtils.find(newAgents,0, newAgents.size()-1, oldAgent) != newAgents.get(newAgents.size()-1))
//                    {
//                        simUI.setMin(simUI.getMin() + 1);
//                    }
//                    else if (ep.getMaxCardinality() > newAgents.size()
//                            && CommonUtils.find(newAss.getUnassignedAgents(), 0, newAss.getUnassignedAgents().size()-1,
//                            oldAgent) != newAss.getUnassignedAgents().lastElement())
//                    {
//                        simUI.setMax(simUI.getMax() + 1);
//                    }
//                }
//            }
//        }
//
//        simUI.setMax(simUI.getMax() + simUI.getMin());
//        // Normalise if possible
//        if (numOldAssignedAgents > 0)
//        {
//            simUI.setMin(simUI.getMin() / numOldAssignedAgents);
//            simUI.setMax(simUI.getMax() / numOldAssignedAgents);
//
//        }
//
//        return simUI;
//    }
//
//    protected UtilityInterval getPriorityResult(IAssignment ass) {
//        this.priResult.setMax(0.0);
//        this.priResult.setMin(0.0);
//        if (this.priorityWeight == 0)
//        {
//            return this.priResult;
//        }
//        //c# != null
//        // SUM UP HEURISTIC PART OF PRIORITY UTILITY
//
//        if (ass.getUnassignedAgents().size() != 0) // == null, when it is a normal assignment
//        {
//            for (long agentID : ass.getUnassignedAgents())
//            {
//
//                this.priResult.setMax(
//                    this.priResult.getMax()
//                    + this.roleHighestPriorityMap.get(this.ra.getRole(agentID).getID()));
//            }
//        }
//        // SUM UP DEFINED PART OF PRIORITY UTILITY
//
//        // for better comparability of different utility functions
//        int denum = Math.min(this.plan.getMaxCardinality(), this.ae.getTeamObserver().teamSize());
//        long taskId;
//        long roleId;
//        //	shared_ptr<vector<EntryPoint*> > eps = ass.getEntryPoints();
//        double curPrio = 0;
//        EntryPoint ep;
//
//        for (int i = 0; i < ass.getEntryPointCount(); ++i) {
//            ep = ass.getEpAgentsMapping().getEntryPoint(i);
//            taskId = ep.getTask().getID();
//            ArrayList<Long> agents = ass.getUniqueAgentsWorkingAndFinished(ep);
//
//            for(long agent : agents) {
//                roleId = this.ra.getRole(agent).getID();
//                this.lookupStruct.taskId = taskId;
//                this.lookupStruct.roleId = roleId;
//                for (TaskRole key : this.priorityMartix.keySet())
//                {
//                    if (key.roleId == this.lookupStruct.roleId
//                        && key.taskId == this.lookupStruct.taskId)
//                    {
//                        curPrio = this.priorityMartix.get(key);
//                        break;
//                    }
//                }
//                if (curPrio < 0.0) // because one Robot has a negative priority for his task
//                {
//                    this.priResult.setMin(-1.0);
//                    this.priResult.setMax(-1.0);
//                    return this.priResult;
//                }
//                this.priResult.setMin(this.priResult.getMin() + curPrio);
////#ifdef UFDEBUG
//                double prio = 0;
//                for(TaskRole key  : this.priorityMartix.keySet())
//                {
//                    if(key.roleId == this.lookupStruct.roleId && key.taskId == this.lookupStruct.taskId)
//                    {
//                        prio = this.priorityMartix.get(key);
//                        break;
//                    }
//                }
//                if (CommonUtils.UFDEBUG_debug) System.out.println("UF: taskId:" + taskId + " roleId:" + roleId + " prio: " + prio);
////#endif
//            }
//        }
////#ifdef UFDEBUG
//        if (CommonUtils.UFDEBUG_debug)System.out.println( "##" );
//        if (CommonUtils.UFDEBUG_debug)System.out.println( "UF: prioUI.Min = " + priResult.getMin());
//        if (CommonUtils.UFDEBUG_debug)System.out.println( "UF: prioUI.Max = " + priResult.getMax() );
//        if (CommonUtils.UFDEBUG_debug)System.out.println( "UF: denum = " + denum );
////#endif
//        priResult.setMax(priResult.getMax() + priResult.getMin());
//        if (denum != 0)
//        {
//            priResult.setMin(priResult.getMin() / denum);
//            priResult.setMax(priResult.getMax() / denum);
//        }
////#ifdef UFDEBUG
//        if (CommonUtils.UFDEBUG_debug)System.out.println( "UF: prioUI.Min = " + priResult.getMin() );
//        if (CommonUtils.UFDEBUG_debug)System.out.println( "UF: prioUI.Max = " + priResult.getMax() );
//        if (CommonUtils.UFDEBUG_debug)System.out.println( "##" );
////#endif
//        return priResult;
//    }
//
//    public void cacheEvalData() {
//        if (this.utilSummands.size() != 0) // == null for default utility function
//        {
//            for (int i = 0; i < this.utilSummands.size(); ++i)
//            {
//                USummand iter = this.utilSummands.get(i);
////                advance(iter, i);
//                (iter).cacheEvalData();
//            }
//        }
//    }
//
//    public void updateAssignment(IAssignment newAss, IAssignment oldAss) {
//        UtilityInterval utilityInterval = this.eval(newAss, oldAss);
//        newAss.setMin(utilityInterval.getMin());
//        newAss.setMax(utilityInterval.getMax());
//    }
//
//    private UtilityInterval eval(IAssignment newAss, IAssignment oldAss) {
//        UtilityInterval sumOfUI = new UtilityInterval(0.0, 0.0);
//        double sumOfWeights = 0.0;
//
//        // Sum up priority summand
//        UtilityInterval prioUI = this.getPriorityResult(newAss);
//        if (prioUI.getMax() == -1.0)
//        {
//            // one robot have a negativ priority for his task . (-1.0, -1.0) for the complete assignment
//            return prioUI;
//        }
//        sumOfUI.setMax(sumOfUI.getMax() + this.priorityWeight * prioUI.getMax());
//        sumOfUI.setMin(sumOfUI.getMin() + this.priorityWeight * prioUI.getMin());
//        sumOfWeights += this.priorityWeight;
//        // Sum up all normal utility summands
//        UtilityInterval curUI;
//        for (int i = 0; i < this.utilSummands.size(); ++i)
//        {
//            USummand iter = utilSummands.get(i);
////            advance(iter, i);
//            curUI = (iter).eval(newAss);
//            //if a summand deny assignment, return -1 for forbidden assignments
//            if (curUI.getMax() == -1.0)
//            {
//                sumOfUI.setMax(-1.0);
//                return sumOfUI;
//            }
//            sumOfWeights += (iter).getWeight();
//            sumOfUI.setMax(sumOfUI.getMax() + (iter).getWeight() * curUI.getMax());
//            sumOfUI.setMin(sumOfUI.getMin() + (iter).getWeight() * curUI.getMin());
//        }
//        if (oldAss != null && this.similarityWeight > 0)
//        {
//            // Sum up similarity summand
//            UtilityInterval simUI = this.getSimilarity(newAss, oldAss);
//            sumOfUI.setMax(sumOfUI.getMax() + this.similarityWeight * simUI.getMax());
//            sumOfUI.setMin(sumOfUI.getMin() + this.similarityWeight * simUI.getMin());
//            sumOfWeights += this.similarityWeight;
//        }
//        if (sumOfWeights > 0.0)
//        {
//            sumOfUI.setMax(sumOfUI.getMax() / sumOfWeights);
//            sumOfUI.setMin(sumOfUI.getMin() / sumOfWeights);
//
//            return sumOfUI;
//        }
//
//        sumOfUI.setMin(0.0);
//        sumOfUI.setMax(0.0);
//        return sumOfUI;
//    }
//
//    public ArrayList<USummand> getUtilSummands() {
//        return utilSummands;
//    }
}
