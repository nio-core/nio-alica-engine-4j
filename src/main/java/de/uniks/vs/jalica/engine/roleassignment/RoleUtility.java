package de.uniks.vs.jalica.engine.roleassignment;

import de.uniks.vs.jalica.engine.collections.AgentProperties;
import de.uniks.vs.jalica.engine.model.Role;
import de.uniks.vs.jalica.engine.teammanagement.Agent;

/**
 * Created by alex on 14.07.17.
 */
public class RoleUtility {
    private final Agent agent;
    private final Role role;
    private final double utilityValue;

    public RoleUtility(double utilityValue, Agent agent, Role role) {
        this.utilityValue = utilityValue;
        this.agent = agent;
        this.role = role;
    }

    public Role getRole() {
        return role;
    }

    public Agent getAgent() {
        return agent;
    }

    public double getUtilityValue() {
        return utilityValue;
    }

//    public int equals(RoleUtility other) {
//
//        if (other.getRole().getID() != this.getRole().getID())
//            return other.getRole().getID() > this.getRole().getID()? 1:-1;
//
//        else if (other.getUtilityValue() != this.getUtilityValue())
//            return other.getUtilityValue() > this.getUtilityValue()? 1:-1;
//
//        else if (other.getAgentProperties().extractID() != this.getAgentProperties().extractID())
//            return other.getAgentProperties().extractID() < this.getAgentProperties().extractID()? 1:-1;
//
//        return 0;
//
//    }

//    public static <T> Comparator<RoleUtility> compareTo() {
//
//        return new Comparator<RoleUtility>() {
//
//            @Override
//            public int compare(RoleUtility o1, RoleUtility o2) {
//                if (o1.getUtilityValue() > o2.getUtilityValue())
//                    return 1;
//                else if (o1.getUtilityValue() == o2.getUtilityValue())
//                    return 0;
//                else
//                    return -1;
//            }
//
//        };
//    }
}
