package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.common.config.ConfigPair;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.supplementary.SystemConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by alex on 14.07.17.
 */
public class RolePriority extends AlicaElement {

    Role role;
    HashMap<Long, Role> roles = new HashMap<>();
    ArrayList<RoleUsage> priorityList = new ArrayList<>();


    public RolePriority(AlicaEngine ae) {

        SystemConfig sc = ae.getSystemConfig();
        this.roles = ae.getPlanRepository().getRoles();

        Vector<String> priorities = new Vector<>(((ConfigPair)sc.get("Globals").get("RolePriority")).getKeys());
//        Vector<String> priorities = (systemConfig)["Globals"].getNames("Globals", "RolePriority", NULL);
        int order = 0;

        for (String priority : priorities)
        {
            order = Integer.valueOf((String) sc.get("Globals").get("RolePriority."+priority));
//            order = (systemConfig)["Globals"].get<int>("Globals", "RolePriority", priority, null);
            for (long key : this.roles.keySet())
            {
                if (this.roles.get(key).getName().equals(priority))
                {
                    System.out.println("RP: Priority  <- " +priority);
                    this.role = this.roles.get(key);
                    break;
                }
            }
            this.priorityList.add(new RoleUsage(order, this.role));
        }
    }

    public ArrayList<RoleUsage> getPriorityList() {
        return priorityList;
    }
}
