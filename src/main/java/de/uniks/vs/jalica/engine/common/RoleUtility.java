package de.uniks.vs.jalica.engine.common;

import de.uniks.vs.jalica.engine.model.Role;
import de.uniks.vs.jalica.engine.collections.AgentProperties;

import java.util.Comparator;

/**
 * Created by alex on 14.07.17.
 */
public class RoleUtility {
    private final AgentProperties agentProperties;
    private final Role role;
    private final double utilityValue;

    public RoleUtility(double utilityValue, AgentProperties agentProperties, Role role) {

        this.utilityValue = utilityValue;
        this.agentProperties = agentProperties;
        this.role = role;
    }

    public Role getRole() {
        return role;
    }

    public AgentProperties getAgentProperties() {
        return agentProperties;
    }


    public int equals(RoleUtility other) {

        if (other.getRole().getID() != this.getRole().getID())
            return other.getRole().getID() < this.getRole().getID()? 1:-1;

        if (other.getUtilityValue() != this.getUtilityValue())
            return other.getUtilityValue() < this.getUtilityValue()? 1:-1;

        if (other.getAgentProperties().extractID() != this.getAgentProperties().extractID())
            return other.getAgentProperties().extractID() < this.getAgentProperties().extractID()? 1:-1;

        return 0;
    }

    public static <T> Comparator<RoleUtility> compareTo() {

        return new Comparator<RoleUtility>() {

            @Override
            public int compare(RoleUtility o1, RoleUtility o2) {
                return o1.equals(o2);
            }

        };
    }

    public double getUtilityValue() {
        return utilityValue;
    }
}
