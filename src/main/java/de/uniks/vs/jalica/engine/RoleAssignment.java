package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.engine.model.Role;
import de.uniks.vs.jalica.engine.model.RoleSet;
import de.uniks.vs.jalica.engine.collections.AgentProperties;
import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.containers.RoleSwitch;
import de.uniks.vs.jalica.engine.common.RoleUtility;

import java.util.*;

/**
 * Created by alex on 13.07.17.
 */
public abstract class RoleAssignment implements IRoleAssignment {

    protected HashMap<Long, Role> agentRoleMapping;
    protected  Vector<RoleUtility> sortedAgentRoleUtility;
    protected AgentProperties ownAgentProperties;
    protected RoleSet roleSet;
    protected  Role ownRole;
    protected  ArrayList<AgentProperties> availableAgents;
    protected  AlicaEngine ae;
    protected  TeamObserver teamObserver;
    protected  HashMap<Long, Role> roles;
    protected AlicaCommunication communication;
    protected  boolean updateRoles;

    public RoleAssignment(AlicaEngine ae) {
        this.ae = ae;
        this.updateRoles = false;
        this.agentRoleMapping = new HashMap<>();
        this.sortedAgentRoleUtility = new Vector<>();
    }

    @Override
    public void init() {
        this.teamObserver = ae.getTeamObserver();
        //TODO delegates missing
//        teamObserver.onTeamCHangedEvent += Update;
        this.ownAgentProperties = teamObserver.getOwnAgentProperties();
//        roleUtilities();
        this.calculateRoles();
    }

    @Override
    public void tick() {

        if (this.updateRoles) {
            this.updateRoles = false;
//            this.roleUtilities();
            this.calculateRoles();
        }
    }

    @Override
    public Role getOwnRole() {
        return ownRole;
    }

    @Override
    public Role getRole(long agentID) {
        Role role = this.agentRoleMapping.get(agentID);

        if (role != null) {
            return role;
        } else {
            role = this.teamObserver.getAgentById(agentID).getCurrentRole();

            if (role != null) {
                return role;
            }
            CommonUtils.aboutError( "RA("+this.ownAgentProperties.extractID()+"): There is no role assigned for agent: " + agentID);
        }
        return null;
    }

    @Override
    public void update() {
        this.updateRoles = true;
    }

    @Override
    public void setCommunication(AlicaCommunication communication) {
        this.communication = communication;
    }

    protected abstract void calculateRoles();

    public void setOwnRole(Role ownRole) {

        if (this.ownRole != ownRole) {
            RoleSwitch rs = new RoleSwitch();
            rs.roleID = ownRole.getID();
            this.communication.sendRoleSwitch(rs);
        }
        this.ownRole = ownRole;
    }
}
