package de.uniks.vs.jalica.engine.roleassignment;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.model.Characteristic;
import de.uniks.vs.jalica.engine.collections.AgentProperties;
import de.uniks.vs.jalica.engine.containers.RoleSwitch;
import de.uniks.vs.jalica.engine.model.Role;

import java.util.Comparator;

/**
 * Created by alex on 13.07.17.
 */
public class DynamicRoleAssignment extends RoleAssignment {

    public DynamicRoleAssignment(AlicaEngine alicaEngine) {
        super(alicaEngine);
    }

    private double calculateMatching(AgentProperties agentProperties, Role role) {
        double matching = 0;
        if (CommonUtils.RA_DEBUG_debug) System.out.println("DRA: Characteristic simiarity " + agentProperties.getName() + "  " + role.getName());

        for ( Characteristic roleCharacteristic : role.getCharacteristics().values()) {
            double similarity = 0;
            String name = null;

            for ( Characteristic agentCharacteristic : agentProperties.getCharacteristics().values()) {
                double _similarity = agentCharacteristic.calculateSimilarityTo(roleCharacteristic) * roleCharacteristic.getWeight();

                if(similarity < _similarity) {
                    similarity = _similarity;
                    name = agentCharacteristic.getName();
                }
            }
            if (CommonUtils.RA_DEBUG_debug) System.out.println("DRA:        characteristic:" + roleCharacteristic.getName()+ "   similarity " + name + " " + similarity);
            matching += similarity;
        }
        matching /= role.getCharacteristics().size();
        return matching;
    }

    protected void calculateRoles() {
        this.roleSet = ae.getRoleSet();

        if (this.roleSet == null)
            CommonUtils.aboutError("DRA(" + this.ownAgentProperties.extractID() + "): The current Roleset is null!");

        this.roles = ae.getPlanRepository().getRoles();
        this.availableAgents = ae.getTeamObserver().getAvailableAgentProperties();
        this.sortedAgentRoleUtility.clear();

        if (CommonUtils.RA_DEBUG_debug) printAvailableAgents();

        for (Role role : this.roles.values()) {

            for (AgentProperties agentProperties : this.availableAgents) {
                double matching = calculateMatching(agentProperties, role);

                if (CommonUtils.RA_DEBUG_debug)  System.out.println("DRA(" + this.ownAgentProperties.extractID() + "): characteristic size:" + agentProperties.getCharacteristics().size() + "    matching " + matching +"\n");

                if (matching != 0) {
                    RoleUtility utility = new RoleUtility(matching, agentProperties, role);
                    this.sortedAgentRoleUtility.add(utility);
//                    Collections.sort(this.sortedAgentRoleUtility, RoleUtility.compareTo());
//                    this.sortedAgentRoleUtility.sort(Comparator.comparingDouble(roleUtility -> roleUtility.getUtilityValue()));
                }
            }
        }
        this.sortedAgentRoleUtility.sort(Comparator.comparingDouble(RoleUtility::getUtilityValue).reversed());
//        double dutility = 0;
//
//        for ( long key : this.roles.keySet()) {
//
//            for (AgentProperties agentProperties : this.availableAgents) {
//                int y = 0;
//                dutility = 0;
//
//                HashMap<String, Characteristic> roleCharacteristics = this.roles.get(key).getCharacteristics();
//
//                if (CommonUtils.RA_DEBUG_debug) System.out.println("RA("+this.ownAgentProperties.extractID()+"): role " + this.roles.get(key).getName());
//
//                for ( String roleCharacteristicKey : roleCharacteristics.keySet()) {
//                    // find the characteristics object of a agent
//                    String roleCharacteristicName = roleCharacteristics.get(roleCharacteristicKey).getName();
//                    HashMap<String, Characteristic> agentPropertiesCharacteristics = agentProperties.getCharacteristics();
//
//                    Characteristic agentCharacteristic = null;
//                    for ( String agentCharacteristicKey : agentPropertiesCharacteristics.keySet()) {
//
//                        if (agentCharacteristicKey.equals(roleCharacteristicName)) {
//                            agentCharacteristic = agentPropertiesCharacteristics.get(agentCharacteristicKey);
//                            break;
//                        }
//                    }
//
//                    if (agentCharacteristic != null) {
//                        Characteristic currentRoleCharacteristic = roleCharacteristics.get(roleCharacteristicKey);
//                        String currentRoleCharacteristicName     = currentRoleCharacteristic.getName();
//                        String currentRoleCharacteristicValue    = currentRoleCharacteristic.getValue();
//
//                        String agentCharacteristicValue  = agentCharacteristic.getValue();
//
//                        double similarity = currentRoleCharacteristic.similarity(agentCharacteristic);
//
//                        if (CommonUtils.RA_DEBUG_debug) System.out.println("RA("+this.ownAgentProperties.extractID()+"): " + roleCharacteristicName + "   agent_value:" + agentCharacteristicValue + "  role_value:" + currentRoleCharacteristicValue);
//
//                        double individualUtility = roleCharacteristics.get(roleCharacteristicKey)
//                                                     .similarityValue( roleCharacteristics.get(roleCharacteristicKey).getValue(),  agentCharacteristic.getValue());
//                        if (individualUtility == 0) {
//                            dutility = 0;
//                            break;
//                        }
//                        dutility += (characteristics.get(roleCharacteristicKey).getWeight() * individualUtility);
//                        y++;
//                    }
//                }
//
//                if (CommonUtils.RA_DEBUG_debug)  System.out.println("RA("+this.ownAgentProperties.extractID()+"): characteristic size:" + agentProperties.getCharacteristics().size() + "    count " + y);
//
//                if (y != 0) {
//                    dutility /= y;
//                    RoleUtility rc = new RoleUtility(dutility, agentProperties, this.roles.get(key));
//                    this.sortedAgentRoleUtility.add(rc);
//                    Collections.sort (this.sortedAgentRoleUtility, RoleUtility.compareTo());
//                }
//            }
//        }

        if (this.sortedAgentRoleUtility.size() == 0) {
            ae.abort("DRA("+this.ownAgentProperties.extractID()+"): Could not establish a mapping between agents and roles. Please check capability definitions!");
        }
        this.agentRoleMapping.clear();
        this.mapRoleToAgent();
        //TODO: remove me
//        RolePriority rolePriority = new RolePriority(ae);
//        int count = 0;
//        while (this.agentRoleMapping.size() < this.availableAgents.size()) {
//            mapRoleToAgent(rolePriority);
//            if ((count++) > 3) {
//                CommonUtils.aboutError("LOOP");
//                System.exit(0);
//            }
//        }
//        rolePriority = null;
    }

    private void mapRoleToAgent() {
//        this.sortedAgentRoleUtility.sort(Comparator.comparing((RoleUtility u) -> u.getRole().getID())
//                                                   .thenComparing(u -> u.getUtilityValue())
//                                                   .thenComparing(u -> u.getAgentProperties().extractID()));

        boolean roleIsAssigned = false;

        for (RoleUtility agentRoleUtility : this.sortedAgentRoleUtility) {
            if (CommonUtils.RA_DEBUG_debug) System.out.println("DRA(" + this.ownAgentProperties.extractID() + "): agentID:" + agentRoleUtility.getAgentProperties().extractID());
            if (CommonUtils.RA_DEBUG_debug) System.out.println("DRA(" + this.ownAgentProperties.extractID() + "):   agent:  " + agentRoleUtility.getAgentProperties().getName() + "  " + agentRoleUtility.getRole().getName());

            if (this.agentRoleMapping.size() != 0
                    && (this.agentRoleMapping.get(agentRoleUtility.getAgentProperties().extractID()) != null
                    || agentRoleUtility.getUtilityValue() == 0)) {
                if (CommonUtils.RA_DEBUG_debug)  System.out.println("DRA(" + this.ownAgentProperties.extractID() + "):     continue (" + this.agentRoleMapping.size() + "!= 0)" + " && " + this.agentRoleMapping.get(agentRoleUtility.getAgentProperties().extractID()) + "!= null" + " || " + agentRoleUtility.getUtilityValue() + "== 0");
                continue;
            }
            if (CommonUtils.RA_DEBUG_debug) System.out.println("DRA(" + this.ownAgentProperties.extractID() + "):     put teamObserver mapping (agentID:" + agentRoleUtility.getAgentProperties().extractID() + " role:" + agentRoleUtility.getRole().getName() + ")");
            this.agentRoleMapping.put(agentRoleUtility.getAgentProperties().extractID(), agentRoleUtility.getRole());

            if (this.ownAgentProperties.extractID() == agentRoleUtility.getAgentProperties().extractID()) {
                if (CommonUtils.RA_DEBUG_debug) System.out.println("DRA(" + this.ownAgentProperties.extractID() + "):       set teamObserver own role (agentID:" + agentRoleUtility.getAgentProperties().extractID() + " role:" + agentRoleUtility.getRole().getName() + ")");
                this.ownRole = agentRoleUtility.getRole();

                if (this.communication != null) {
                    RoleSwitch roleSwitch = new RoleSwitch();
                    roleSwitch.roleID = this.ownRole.getID();
                    this.communication.sendRoleSwitch(roleSwitch);
                }
            }
            this.teamObserver.getAgentById(agentRoleUtility.getAgentProperties().extractID()).setCurrentRole(agentRoleUtility.getRole());
            roleIsAssigned = true;
            break;
        }
        if (!roleIsAssigned) {
            ae.abort("RA: Could not set a role for agent " + this.ownAgentProperties.getName() + " with default role " + this.ownAgentProperties.getDefaultRole() + "!");
        }
    }

// -- debug outputs ------
    private void printAvailableAgents() {
        System.out.print("DRA("+this.ownAgentProperties.extractID()+"): Available agents: " + this.availableAgents.size());
        System.out.print("   agent Ids: ");

        for (AgentProperties agentProperties : this.availableAgents) {
            System.out.print(agentProperties.extractID() + ":" + agentProperties.getName() + " , ");
        }
        System.out.println();
    }
}
