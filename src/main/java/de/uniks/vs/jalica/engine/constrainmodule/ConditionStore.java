package de.uniks.vs.jalica.engine.constrainmodule;

import de.uniks.vs.jalica.engine.RunningPlan;
import de.uniks.vs.jalica.engine.model.Condition;
import de.uniks.vs.jalica.engine.model.Variable;
import de.uniks.vs.jalica.engine.common.CommonUtils;

import java.util.*;

/**
 * Created by alex on 17.07.17.
 */
public class ConditionStore {


    private LinkedHashMap<Variable, Vector<Condition>> activeVar2CondMap = new LinkedHashMap<>();
    private ArrayList<Condition> activeConditions = new ArrayList<>();

    public void addCondition(Condition con)
    {
        if (con == null || (con.getVariables().size() == 0 && con.getQuantifiers().size() == 0))
        {
            return;
        }

        boolean modified = false;
//        mtx.lock();
//        if (find(activeConditions.begin(), activeConditions.end(), con) == activeConditions.end())
        if (CommonUtils.find(activeConditions, 0, activeConditions.size()-1, con) == activeConditions.get(activeConditions.size()-1))
        {
            modified = true;
            activeConditions.add(con);
        }
//        mtx.unlock();
        if (modified)
        {
            for (Variable variable : con.getVariables())
            {
                Vector<Condition> it = activeVar2CondMap.get(variable);

                if (it != activeVar2CondMap.entrySet().toArray()[activeVar2CondMap.size() -1])
//                    if (it != activeVar2CondMap.end())
                {
                    it.add(con);
                }
                else
                {
                    Vector<Condition> condList = new Vector<Condition>();
                    condList.add(con);
                    activeVar2CondMap.put(variable, condList);
                }
            }
        }
//#ifdef CS_DEBUG
        if (CommonUtils.CS_DEBUG_debug) System.out.println("CS: Added condition in " + con.getAbstractPlan().getName() + " with " + con.getVariables().size() + " vars" );
//#endif
    }

    public void clear() {
        activeVar2CondMap.clear();
//        mtx.lock();
        activeConditions.clear();
//        mtx.unlock();
    }

    void acceptQuery(Query query, RunningPlan rp) {
        CommonUtils.aboutNoImpl();
    }
}
