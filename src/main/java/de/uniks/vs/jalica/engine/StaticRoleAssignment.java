package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.engine.model.Role;
import de.uniks.vs.jalica.engine.collections.AgentProperties;
import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.containers.RoleSwitch;

/**
 * Created by alex on 13.07.17.
 */
public class StaticRoleAssignment extends RoleAssignment {

    public StaticRoleAssignment(AlicaEngine alicaEngine) {
        super(alicaEngine);
    }

    protected void calculateRoles() {
        this.agentRoleMapping.clear();
        this.roles = ae.getPlanRepository().getRoles();
        this.availableAgents = ae.getTeamObserver().getAvailableAgentProperties();

        for (AgentProperties agent : this.availableAgents) {
            boolean roleIsAssigned = false;

            for (Long roleID : this.roles.keySet()) {
                Role role = this.roles.get(roleID);

                if (role.getName().equals(agent.getDefaultRole())) {
                    if (CommonUtils.RA_DEBUG_debug)  System.out.println("RA: Setting Role " + role.getName() + " for agent ID " + agent.extractID());
                    this.agentRoleMapping.put(agent.extractID(), role);
                    this.to.getAgentById(agent.extractID()).setLastRole(role);

                    if (agent.extractID() == this.to.getOwnID() && this.ownRole != role) {
                        this.ownRole = role;

                        if (this.communication != null) {
                            RoleSwitch rs = new RoleSwitch();
                            rs.roleID = roleID;
                            this.communication.sendRoleSwitch(rs);
                        }
                    }
                    roleIsAssigned = true;
                    break;
                }
            }

            if (!roleIsAssigned) {
                ae.abort("RA: Could not set a role for agent " + agent.getName() + " with default role " + agent.getDefaultRole() + "!");
            }
        }
    }
}
