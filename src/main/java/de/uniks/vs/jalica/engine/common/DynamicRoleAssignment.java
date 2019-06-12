package de.uniks.vs.jalica.engine.common;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.RoleAssignment;
import de.uniks.vs.jalica.engine.collections.AgentProperties;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by alex on 13.07.17.
 */
public class DynamicRoleAssignment extends RoleAssignment {

    public DynamicRoleAssignment(AlicaEngine alicaEngine) {
        super(alicaEngine);
    }

    protected void calculateRoles() {
        this.roleSet = ae.getRoleSet();
        this.roles = ae.getPlanRepository().getRoles();

        if (this.roleSet == null) {
            System.err.println( "RA("+this.ownAgentProperties.extractID()+"): The current Roleset is null!" );
            try {
                throw new Exception();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.availableAgents = ae.getTeamObserver().getAvailableAgentProperties();

        if (CommonUtils.RA_DEBUG_debug) {
            System.out.print("RA("+this.ownAgentProperties.extractID()+"): Available agents: " + this.availableAgents.size());
            System.out.print("   agent Ids: ");

            for (AgentProperties agentProperties : this.availableAgents) {
                System.out.print(agentProperties.extractID() + ":" + agentProperties.getName() + " , ");
            }
            System.out.println();
        }
        double dutility = 0;
        this.sortedAgents.clear();

        for ( long key : this.roles.keySet()) {

            for (AgentProperties robProperties : this.availableAgents) {
                int y = 0;
                dutility = 0;

                // TODO: MOTIVATION HACK
                if (this.roles.get(key).getName().endsWith(robProperties.getDefaultRole())) {
                    dutility = 1.0;
                    y = 1;
                }
                // TODO: ----------------

                HashMap<String, Characteristic> characteristics = this.roles.get(key).getCharacteristics();

                if (CommonUtils.RA_DEBUG_debug) System.out.println("RA("+this.ownAgentProperties.extractID()+"): " + this.roles.get(key).getName());

                for ( String roleCharacKey : characteristics.keySet()) {
                    // find the characteristics object of a agent
                    Characteristic rbChar = null;
                    String roleCharacName = characteristics.get(roleCharacKey).getName(); // roleCharacEntry.second.getName();

                    HashMap<String, Characteristic> robPropertiesCharacteristics = robProperties.getCharacteristics();

                    for ( String agentCharacKey : robPropertiesCharacteristics.keySet()) {

                        if (agentCharacKey.equals(roleCharacName)) {
                            rbChar = robPropertiesCharacteristics.get(agentCharacKey); // agentCharac.second;
                            break;
                        }
                    }

                    if (rbChar != null) {
                        Characteristic characteristic       = characteristics.get(roleCharacKey);
                        String capability               = characteristic.getName();
                        String characteristic1CapValue    = characteristic.getValue();
                        String charCapValue               = rbChar.getValue();
                        double v                            = characteristic.similarityValue(characteristic1CapValue, charCapValue);
                        if (CommonUtils.RA_DEBUG_debug) System.out.println("RA("+this.ownAgentProperties.extractID()+"): " + roleCharacName + "   " + charCapValue + "  " + characteristic1CapValue);

                        double individualUtility = characteristics.get(roleCharacKey)/*roleCharacEntry.second*/.similarityValue(
                                characteristics.get(roleCharacKey).getValue(),  rbChar.getValue());
                        if (individualUtility == 0) {
                            dutility = 0;
                            break;
                        }
                        dutility += (characteristics.get(roleCharacKey).getWeight() * individualUtility);
                        y++;
                    }
                }

                if (CommonUtils.RA_DEBUG_debug)  System.out.println("RA("+this.ownAgentProperties.extractID()+"): chacteristic size:" + robProperties.getCharacteristics().size() + "    count " + y);

                if (y != 0) {
                    dutility /= y;
                    RoleUtility rc = new RoleUtility(dutility, robProperties, this.roles.get(key));
                    this.sortedAgents.add(rc);
                    Collections.sort (this.sortedAgents, RoleUtility.compareTo());
                }
            }
        }

        if (this.sortedAgents.size() == 0) {
            ae.abort("RA("+this.ownAgentProperties.extractID()+"): Could not establish a mapping between agents and roles. Please check capability definitions!");
        }
        RolePriority rp = new RolePriority(ae);
        this.agentRoleMapping.clear();

        //TODO: remove me
        int count = 0;
        while (this.agentRoleMapping.size() < this.availableAgents.size()) {
            mapRoleToAgent(rp);
            if ((count++) > 3) {
                CommonUtils.aboutError("LOOP");
                System.exit(0);
            }
        }
        rp = null;
    }

    private void mapRoleToAgent(RolePriority rp) {

        this.sortedAgents.sort(new Comparator<RoleUtility>() {
            @Override
            public int compare(RoleUtility thisOne, RoleUtility otherOne) {
                if(otherOne.getRole().getID() != thisOne.getRole().getID())
                    return otherOne.getRole().getID() < thisOne.getRole().getID() ? -1 :1;

                if(otherOne.getUtilityValue() != thisOne.getUtilityValue())
                    return otherOne.getUtilityValue() < thisOne.getUtilityValue() ? -1 :1;

                if(otherOne.getAgentProperties().extractID() != thisOne.getAgentProperties().extractID())
                    return otherOne.getAgentProperties().extractID() < thisOne.getAgentProperties().extractID() ? -1 :1;

                return 0;
            }
        });

        for (RoleUsage roleUsage : rp.getPriorityList()) {
            //if (CommonUtils.RA_DEBUG_debug) System.out.println("RA("+this.ownAgentProperties.extractID()+"): role:" + roleUsage.getRole().getName() );
            for (RoleUtility agentRoleUtil : this.sortedAgents) {
                //if (CommonUtils.RA_DEBUG_debug) System.out.println("RA("+this.ownAgentProperties.extractID()+"):    agentID:" + agentRoleUtil.getAgentProperties().extractID());
                //if (CommonUtils.RA_DEBUG_debug) System.out.println("RA("+this.ownAgentProperties.extractID()+"):    agent:" + agentRoleUtil.getAgentProperties().getName() + " " + roleUsage.getRole().getName() +"  "+ agentRoleUtil.getRole().getName());

                if (roleUsage.getRole() == agentRoleUtil.getRole()) {
                    //if (CommonUtils.RA_DEBUG_debug) System.out.println("RA("+this.ownAgentProperties.extractID()+"):      role:" + roleUsage.getRole().getName() + " is equal");
                    if (this.agentRoleMapping.size() != 0
                            && (this.agentRoleMapping.get(agentRoleUtil.getAgentProperties().extractID()) != null
                            || agentRoleUtil.getUtilityValue() == 0)) {
                        if (CommonUtils.RA_DEBUG_debug) System.out.println("RA("+this.ownAgentProperties.extractID()+"):        continue (" + this.agentRoleMapping.size()+ "!= 0)" +" && "+ this.agentRoleMapping.get(agentRoleUtil.getAgentProperties().extractID()) +"!= null" +" || "+ agentRoleUtil.getUtilityValue() + "== 0");
                        continue;
                    }
                    if (CommonUtils.RA_DEBUG_debug) System.out.println("RA("+this.ownAgentProperties.extractID()+"):          put to mapping (agentID:" + agentRoleUtil.getAgentProperties().extractID() +" role:" + agentRoleUtil.getRole().getName() +")");
                    this.agentRoleMapping.put(agentRoleUtil.getAgentProperties().extractID(), agentRoleUtil.getRole());

                    if (this.ownAgentProperties.extractID() == agentRoleUtil.getAgentProperties().extractID())
                    {
                        if (CommonUtils.RA_DEBUG_debug) System.out.println("RA("+this.ownAgentProperties.extractID()+"):            set to own role (agentID:" + agentRoleUtil.getAgentProperties().extractID() +" role:" + agentRoleUtil.getRole().getName()+")");
                        this.ownRole = agentRoleUtil.getRole();
                    }

                    to.getAgentById(agentRoleUtil.getAgentProperties().extractID()).setLastRole(agentRoleUtil.getRole());

                    break;
                }
                else
                if (CommonUtils.RA_DEBUG_debug) System.out.println("RA("+this.ownAgentProperties.extractID()+"):    fail (agentID:" + agentRoleUtil.getAgentProperties().extractID() +" role:" + roleUsage.getRole().getName() +")");
            }
        }
    }
}
