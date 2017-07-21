package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.common.AssignmentCollection;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by alex on 21.07.17.
 */
public class PartialAssignment implements IAssignment{


    public static void reset(PartialAssignmentPool pap) {
        pap.curIndex = 0;
    }

    public static PartialAssignment getNew(PartialAssignmentPool pap, Vector<Integer> robots, Plan plan, SuccessCollection sucCol) {
        if (pap.curIndex >= pap.maxCount)
        {
            System.out.println( "max PA count reached!" );
        }
        PartialAssignment ret = pap.daPAs[pap.curIndex++];
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
        for ( iter : plan.getEntryPoints())
        {
            ret.epRobotsMapping.setEp(j++, iter.second);
        }

        // Sort the entrypoint array
        ret.epRobotsMapping.sortEps();


        for (int i = 0; i < ret.epRobotsMapping.getSize(); i++)
        {
            ret.dynCardinalities[i].setMin(ret.epRobotsMapping.getEp(i).getMinCardinality());
            ret.dynCardinalities[i].setMax(ret.epRobotsMapping.getEp(i).getMaxCardinality());
            ArrayList<Integer> suc = sucCol.getRobots(ret.epRobotsMapping.getEp(i));

            if (suc != null)
            {
                ret.dynCardinalities[i].setMin(ret.dynCardinalities[i].getMin() - suc.size());
                ret.dynCardinalities[i].setMax(ret.dynCardinalities[i].getMax() - suc.size());
                if (ret.dynCardinalities[i].getMin() < 0)
                {
                    ret.dynCardinalities[i].setMin(0);
                }
                if (ret.dynCardinalities[i].getMax() < 0)
                {
                    ret.dynCardinalities[i].setMax(0);
                }

//#ifdef SUCDEBUG
                System.out.println("SuccessCollection" );
                System.out.println( "EntryPoint: " << ret.epRobotsMapping.getEntryPoints().at(i).toString() );
                System.out.println( "DynMax: " << ret.dynCardinalities[i].getMax() );
                System.out.println( "DynMin: " << ret.dynCardinalities[i].getMin() );
                System.out.print("SucCol: ");
                for (int k : (suc))
                {
                    System.out.print( k + ", ");
                }
                System.out.println( "-----------" );
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

}
