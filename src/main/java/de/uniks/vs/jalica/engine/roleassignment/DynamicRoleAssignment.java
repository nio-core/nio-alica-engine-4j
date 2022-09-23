package de.uniks.vs.jalica.engine.roleassignment;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.IRoleAssignment;
import de.uniks.vs.jalica.engine.common.SystemConfig;
import de.uniks.vs.jalica.engine.idmanagement.ID;
import de.uniks.vs.jalica.engine.model.Characteristic;
import de.uniks.vs.jalica.engine.collections.AgentProperties;
import de.uniks.vs.jalica.engine.containers.RoleSwitch;
import de.uniks.vs.jalica.engine.model.Role;
import de.uniks.vs.jalica.engine.model.RoleSet;
import de.uniks.vs.jalica.engine.teammanagement.Agent;

import java.util.*;

/**
 * Created by alex on 13.07.17.
 * Updated 23.6.19
 */
public class DynamicRoleAssignment extends IRoleAssignment {

    private AlicaEngine ae;
    private boolean updateRoles;

    private HashMap<ID, Agent> activeAgents;
    private ArrayList<RoleUtility> sortedAgentRoleUtility;
    private Agent localAgent;


    public DynamicRoleAssignment(AlicaEngine alicaEngine) {
        super();
        this.updateRoles = false;
        this.ae = alicaEngine;
        this.localAgent = null;
        this.sortedAgentRoleUtility = new ArrayList<>();

    }

    public void init() {
        this.localAgent = this.ae.getTeamManager().getLocalAgent();
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

    private double calculateMatching(AgentProperties agentProperties, Role role) {
        double matching = 0;
        if (CommonUtils.RA_DEBUG_debug) System.out.println("DRA: Characteristic simiarity " + agentProperties.getName() + "  " + role.getName());

        for ( Characteristic roleCharacteristic : role.getCharacteristics().values()) {
            double similarity = 0;
            String name = null;

            for ( Characteristic agentCharacteristic : agentProperties.getCharacteristics().values()) {

                if (agentCharacteristic.getValue() == null || roleCharacteristic.getValue() == null)
                    continue;
                double _similarity = agentCharacteristic.calculateSimilarityTo(roleCharacteristic) * roleCharacteristic.getWeight();

                if(similarity < _similarity) {
                    similarity = _similarity;
                    name = agentCharacteristic.getName();
                }
            }
            if (CommonUtils.RA_DEBUG_debug) System.out.println("DRA:        characteristic:" + roleCharacteristic.getName()+ "   similarity " + name + " " + similarity);
            matching += similarity;
        }
        matching = role.getCharacteristics().size() > 0 ? matching/role.getCharacteristics().size(): 0.0000000001;
        return matching;
    }

    protected void calculateRoles() {
        String name = this.localAgent.getName();
        SystemConfig sc = this.ae.getSystemConfig();
        RoleSet roleSet = ae.getRoleSet();

        if (roleSet == null)
            CommonUtils.aboutError("DRA(" + this.localAgent.getProperties().extractID(name, sc) + "): The current Roleset is null!");

        LinkedHashMap<Long, Role> roles = ae.getPlanRepository().getRoles();
        this.activeAgents = ae.getTeamManager().getActiveAgents();
        this.sortedAgentRoleUtility.clear();

        if (CommonUtils.RA_DEBUG_debug) printAvailableAgents();

        for (Agent agent : this.activeAgents.values()) {

            for (Role role : roles.values()) {
                double matching = calculateMatching(agent.getProperties(), role);

                if (CommonUtils.RA_DEBUG_debug)  System.out.println("DRA(" + this.localAgent.getProperties().extractID(name, sc) + "): characteristic size:" + agent.getProperties().getCharacteristics().size() + "    matching " + matching);

                if (matching != 0) {
                    RoleUtility utility = new RoleUtility(matching, agent, role);
                    this.sortedAgentRoleUtility.add(utility);
//                    Collections.sort(this.sortedAgentRoleUtility, RoleUtility.compareTo());
//                    this.sortedAgentRoleUtility.sort(Comparator.comparingDouble(roleUtility -> roleUtility.getUtilityValue()));
                }
            }
        }
        this.sortedAgentRoleUtility.sort(Comparator.comparingDouble(RoleUtility::getUtilityValue).reversed());

        if (this.sortedAgentRoleUtility.size() == 0) {
            System.out.println("DRA("+this.localAgent.getProperties().extractID(name, sc)+"): Could not establish a mapping between agents and roles. Please check capability definitions!");
        }
        this.robotRoleMapping.clear();
        this.mapRoleToAgent();
    }

    private void mapRoleToAgent() {
        String name = this.localAgent.getName();
        SystemConfig sc = this.ae.getSystemConfig();
//        this.sortedAgentRoleUtility.sort(Comparator.comparing((RoleUtility u) -> u.getRole().getID())
//                                                   .thenComparing(u -> u.getUtilityValue())
//                                                   .thenComparing(u -> u.getAgentProperties().extractID()));

        boolean roleIsAssigned = false;

        for (RoleUtility agentRoleUtility : this.sortedAgentRoleUtility) {
            if (CommonUtils.RA_DEBUG_debug) System.out.println("DRA(" + this.localAgent.getProperties().extractID(name, sc) + "): agentID:" + agentRoleUtility.getAgent().getProperties().extractID(name, sc));
            if (CommonUtils.RA_DEBUG_debug) System.out.println("DRA(" + this.localAgent.getProperties().extractID(name, sc) + "):   agent:  " + agentRoleUtility.getAgent().getProperties().getName() + "  " + agentRoleUtility.getRole().getName());

            if (this.robotRoleMapping.size() != 0
                    && (this.robotRoleMapping.get(agentRoleUtility.getAgent().getProperties().extractID(name, sc)) != null
                    || agentRoleUtility.getUtilityValue() == 0)) {
                if (CommonUtils.RA_DEBUG_debug)  System.out.println("DRA(" + this.localAgent.getProperties().extractID(name, sc) + "):     continue (" + this.robotRoleMapping.size() + "!= 0)" + " && " + this.robotRoleMapping.get(agentRoleUtility.getAgent().getProperties().extractID(name, sc)) + "!= null" + " || " + agentRoleUtility.getUtilityValue() + "== 0");
                continue;
            }
            if (CommonUtils.RA_DEBUG_debug) System.out.println("DRA(" + this.localAgent.getProperties().extractID(name, sc) + "):     put teamObserver mapping (agentID:" + agentRoleUtility.getAgent().getProperties().extractID(name, sc) + " role:" + agentRoleUtility.getRole().getName() + ")");


            this.robotRoleMapping.put(agentRoleUtility.getAgent().getProperties().extractID(name, sc), agentRoleUtility.getRole());

            if (this.localAgent.getProperties().extractID(name, sc) == agentRoleUtility.getAgent().getProperties().extractID(name, sc)) {
                if (CommonUtils.RA_DEBUG_debug) System.out.println("DRA(" + this.localAgent.getProperties().extractID(name, sc) + "):       set teamObserver own role (agentID:" + agentRoleUtility.getAgent().getProperties().extractID(name, sc) + " role:" + agentRoleUtility.getRole().getName() + ")");
                this.ownRole = agentRoleUtility.getRole();

                if (this.communication != null) {
                    RoleSwitch roleSwitch = new RoleSwitch();
                    roleSwitch.roleID = this.ownRole.getID();
                    this.communication.sendRoleSwitch(roleSwitch);
                }
//            this.teamObserver.getAgentById(agentRoleUtility.getAgent().getProperties().extractID()).setCurrentRole(agentRoleUtility.getRole());
//            Agent agent = this.ae.getTeamManager().getAgentByID(agentRoleUtility.getAgent().getProperties().extractID(name, sc));
//            agent.getProperties().setCurrentRole(agentRoleUtility.getRole());
                roleIsAssigned = true;
                break;
            }
        }
        if (!roleIsAssigned) {
            System.out.println("DRA: Could not set a role for agent " + this.localAgent.getProperties().getName() + " with default role " + this.localAgent.getProperties().getDefaultRole() + "!");
        }
    }

// -- debug outputs ------
    private void printAvailableAgents() {
        String name = this.localAgent.getName();
        SystemConfig sc = this.ae.getSystemConfig();
        System.out.print("DRA("+this.localAgent.getProperties().extractID(name, sc)+"): Available agents: " + this.activeAgents.size());
        System.out.print("   agent Ids: " + "    active " + this.activeAgents.size() + " agents");
        System.out.println("\nDRA("+this.localAgent.getProperties().extractID(name, sc)+"): " + this.activeAgents.size());
        System.out.println("DRA("+this.localAgent.getProperties().extractID(name, sc)+"): " + this.activeAgents.keySet());

        for (Agent agent : this.activeAgents.values()) {
            System.out.print(agent.getProperties().extractID(name, sc) + ":" + agent.getProperties().getName() + " , ");
        }
        System.out.println();
    }
}
