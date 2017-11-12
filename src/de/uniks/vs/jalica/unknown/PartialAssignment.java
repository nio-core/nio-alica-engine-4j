package de.uniks.vs.jalica.unknown;

import com.sun.org.apache.bcel.internal.generic.ARRAYLENGTH;
import de.uniks.vs.jalica.common.AssignmentCollection;
import de.uniks.vs.jalica.common.UtilityFunction;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by alex on 21.07.17.
 */
public class PartialAssignment implements IAssignment{


    private static final long PRECISION = 1073741824;
    private double min;
    private double max;
    private long compareVal;
    private Vector<Integer> unassignedRobots;
    private AssignmentCollection epRobotsMapping;
    private boolean hashCalculated;
    private Vector<Integer> robots;
    private Plan plan;
    private UtilityFunction utilFunc;
    private SuccessCollection epSuccessMapping;
    private Vector<DynCardinality> dynCardinalities;

    public static void reset(PartialAssignmentPool pap) {
        pap.curIndex = 0;
    }

    public static PartialAssignment getNew(PartialAssignmentPool pap, Vector<Integer> robots, Plan plan, SuccessCollection sucCol) {
        if (pap.curIndex >= pap.maxCount)
        {
            System.out.println( "PA: max PA count reached!" );
        }
        PartialAssignment ret = pap.daPAs.get(pap.curIndex++);
        ret.clear();
        ret.robots = robots; // Should already be sorted! (look at TaskAssignment, or PlanSelector)
        ret.plan = plan;
        ret.utilFunc = plan.getUtilityFunction();
        ret.epSuccessMapping = sucCol;
        // Create EP-Array
        if (AssignmentCollection.allowIdling)
        {
            ret.epRobotsMapping.setSize(plan.getEntryPoints().size() + 1);
            // Insert IDLE-EntryPoint
            ret.epRobotsMapping.setEp(ret.epRobotsMapping.getSize() - 1, pap.idleEP);
        }
        else
        {
            ret.epRobotsMapping.setSize(plan.getEntryPoints().size());
        }
        // Insert plan entrypoints
        int j = 0;
        for ( Long key : plan.getEntryPoints().keySet())
        {
            ret.epRobotsMapping.setEp(j++, plan.getEntryPoints().get(key));
        }

        // Sort the entrypoint array
        ret.epRobotsMapping.sortEps();


        for (int i = 0; i < ret.epRobotsMapping.getSize(); i++)
        {
            ret.dynCardinalities.get(i).setMin(ret.epRobotsMapping.getEp(i).getMinCardinality());
            ret.dynCardinalities.get(i).setMax(ret.epRobotsMapping.getEp(i).getMaxCardinality());
            ArrayList<Integer> suc = sucCol.getRobots(ret.epRobotsMapping.getEp(i));

            if (suc != null)
            {
                ret.dynCardinalities.get(i).setMin(ret.dynCardinalities.get(i).getMin() - suc.size());
                ret.dynCardinalities.get(i).setMax(ret.dynCardinalities.get(i).getMax() - suc.size());
                if (ret.dynCardinalities.get(i).getMin() < 0)
                {
                    ret.dynCardinalities.get(i).setMin(0);
                }
                if (ret.dynCardinalities.get(i).getMax() < 0)
                {
                    ret.dynCardinalities.get(i).setMax(0);
                }

//#ifdef SUCDEBUG
                if (CommonUtils.SUCDEBUG_debug)  System.out.println("PA: SuccessCollection" );
                if (CommonUtils.SUCDEBUG_debug)System.out.println( "PA: EntryPoint: " + ret.epRobotsMapping.getEntryPoints().get(i).toString() );
                if (CommonUtils.SUCDEBUG_debug)System.out.println( "PA: DynMax: " + ret.dynCardinalities.get(i).getMax() );
                if (CommonUtils.SUCDEBUG_debug)System.out.println( "PA: DynMin: " + ret.dynCardinalities.get(i).getMin() );
                if (CommonUtils.SUCDEBUG_debug)System.out.print("PA: SucCol: ");
                for (int k : (suc))
                {
                    if (CommonUtils.SUCDEBUG_debug)System.out.print( k + ", ");
                }
                if (CommonUtils.SUCDEBUG_debug)System.out.println( "-----------" );
//#endif
            }
        }

        // At the beginning all robots are unassigned
        for (int i : robots)
        {
            ret.unassignedRobots.add(i);
        }
        return ret;
    }

    private void clear() {
        this.min = 0.0;
        this.max = 1.0;
        this.compareVal = PRECISION;
        this.unassignedRobots.clear();
        for (int i = 0; i < this.epRobotsMapping.getSize(); i++)
        {
            this.epRobotsMapping.getRobots(i).clear();
        }
        this.hashCalculated = false;
    }

    @Override
    public int getEntryPointCount() {
        return 0;
    }

    @Override
    public ArrayList<Integer> getRobotsWorkingAndFinished(EntryPoint ep) {
        CommonUtils.aboutNoImpl();
        return null;
    }

    @Override
    public ArrayList<Integer> getUniqueRobotsWorkingAndFinished(EntryPoint ep) {
        CommonUtils.aboutNoImpl();
        return null;
    }

    @Override
    public void setMin(double min) {
        CommonUtils.aboutNoImpl();
    }

    @Override
    public void setMax(double max) {
        CommonUtils.aboutNoImpl();
    }

    @Override
    public Vector<Integer> getUnassignedRobots() {
        CommonUtils.aboutNoImpl();
        return null;
    }

    @Override
    public AssignmentCollection getEpRobotsMapping() {
        CommonUtils.aboutNoImpl();
        return null;
    }

    public boolean addIfAlreadyAssigned(SimplePlanTree spt, int robot) {
        if (spt.getEntryPoint().getPlan() == this.plan)
        {
            EntryPoint curEp;
            int max = this.epRobotsMapping.getSize();
            if (AssignmentCollection.allowIdling)
            {
                max--;
            }
            for (int i = 0; i < max; ++i)
            {
                curEp = this.epRobotsMapping.getEp(i);
                if (spt.getEntryPoint().getId() == curEp.getId())
                {
                    if (!this.assignRobot(robot, i))
                    {
                        break;
                    }
                    //remove robot from "To-Add-List"
                    Integer iter = CommonUtils.find(this.unassignedRobots, 0, this.unassignedRobots.size() - 1, robot);
                    if (this.unassignedRobots.remove(this.unassignedRobots.indexOf(iter)) == this.unassignedRobots.lastElement())
                    {
                        System.err.println( "PA: Tried to assign robot " + robot + ", but it was NOT UNassigned!");
                        try {
                            throw new Exception();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    //return true, because we are ready, when we found the robot here
                    return true;
                }
            }
            return false;
        }
        // If there are children and we didnt find the robot until now, then go on recursive
		else if (spt.getChildren().size() > 0)
        {
            for (SimplePlanTree sptChild : spt.getChildren())
            {
                if (this.addIfAlreadyAssigned(sptChild, robot))
                {
                    return true;
                }
            }
        }
        // Did not find the robot in any relevant entry point
        return false;
    }

    private boolean assignRobot(int robot, int index) {
        if (this.dynCardinalities.get(index).getMax() > 0)
        {
            this.epRobotsMapping.getRobots(index).add(robot);
            if (this.dynCardinalities.get(index).getMin() > 0)
            {
                this.dynCardinalities.get(index).setMin(this.dynCardinalities.get(index).getMin() - 1);
            }
            if (this.dynCardinalities.get(index).getMax() <= Integer.MAX_VALUE)
            {
                this.dynCardinalities.get(index).setMax(this.dynCardinalities.get(index).getMax() - 1);
            }
            return true;
        }
        return false;
    }
}
