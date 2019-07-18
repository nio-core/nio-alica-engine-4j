package de.uniks.vs.jalica.engine.roleassignment;

import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.IRoleAssignment;
import de.uniks.vs.jalica.engine.model.Role;
import de.uniks.vs.jalica.engine.collections.AgentProperties;
import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.containers.RoleSwitch;
import de.uniks.vs.jalica.engine.teammanagement.Agent;

import java.util.LinkedHashMap;

import static java.lang.System.getProperties;

/**
 * Created by alex on 13.07.17.
 */
public class StaticRoleAssignment extends IRoleAssignment {

    private AlicaEngine ae;
    private boolean updateRoles;

    public StaticRoleAssignment(AlicaEngine alicaEngine) {
        super();
        this.ae = alicaEngine;
        this.updateRoles = false;
    }

    public void init() {
        this.calculateRoles();
    }

    public void tick() {
        if (this.updateRoles) {
            this.updateRoles = false;
            this.calculateRoles();
        }
    }

    public void update() {
        this.updateRoles = true;
    }


    protected void calculateRoles() {
        // clear current map
        this.robotRoleMapping.clear();

        // get data for "calculations"
        LinkedHashMap<Long, Role> roles = ae.getPlanRepository().getRoles();

        // assign a role for each robot if you have match
        for ( Agent agent : ae.getTeamManager().getActiveAgents().values()) {
            AgentProperties prop = agent.getProperties();
            boolean roleIsAssigned = false;

            for ( Role role : roles.values()) {
                // make entry in the map if the roles match
                if (role.getName().equals(prop.getDefaultRole())) {
                    System.out.println("Static RA: Setting Role " + role.getName() + " for robot ID " + agent.getId());
                    this.robotRoleMapping.put(agent.getId(), role);

                    // set own role, if its me
                    if (agent.getId() == this.ae.getTeamManager().getLocalAgentID() && this.ownRole != role) {
                        this.ownRole = role;
                        // probably nothing is reacting on this message, but anyway we send it
                        if (this.communication != null) {
                            RoleSwitch rs = new RoleSwitch();
                            rs.roleID = role.getID();
                            this.communication.sendRoleSwitch(rs);
                        }
                    }
                    roleIsAssigned = true;
                    break;
                }
            }

            if (!roleIsAssigned) {
                CommonUtils.aboutError("RA: Could not set a role (Default: " + prop.getDefaultRole() + ") for robot: " + agent.getId());
            }
        }
    }
}
