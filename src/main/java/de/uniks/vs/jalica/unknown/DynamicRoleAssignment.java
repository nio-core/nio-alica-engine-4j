package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.engine.AlicaEngine;

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
            System.err.println( "RA("+this.ownAgentProperties.getID()+"): The current Roleset is null!" );
            try {
                throw new Exception();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.availableAgents = ae.getTeamObserver().getAvailableAgentProperties();

        if (CommonUtils.RA_DEBUG_debug) {
            System.out.print("RA("+this.ownAgentProperties.getID()+"): Available agents: " + this.availableAgents.size());
            System.out.print("   agent Ids: ");

            for (AgentProperties agentProperties : this.availableAgents) {
                System.out.print(agentProperties.getID() + ":" + agentProperties.getName() + " , ");
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

                if (CommonUtils.RA_DEBUG_debug) System.out.println("RA("+this.ownAgentProperties.getID()+"): " + this.roles.get(key).getName());

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
                        Capability capability               = characteristic.getCapability();
                        CapValue characteristic1CapValue    = characteristic.getCapValue();
                        CapValue charCapValue               = rbChar.getCapValue();
                        double v                            = capability.similarityValue(characteristic1CapValue, charCapValue);
                        if (CommonUtils.RA_DEBUG_debug) System.out.println("RA("+this.ownAgentProperties.getID()+"): " + roleCharacName + "   " + charCapValue.getName() + "  " + characteristic1CapValue.getName());

                        double individualUtility = characteristics.get(roleCharacKey)/*roleCharacEntry.second*/.getCapability().similarityValue(
                                characteristics.get(roleCharacKey).getCapValue(),  rbChar.getCapValue());
                        if (individualUtility == 0) {
                            dutility = 0;
                            break;
                        }
                        dutility += (characteristics.get(roleCharacKey).getWeight() * individualUtility);
                        y++;
                    }
                }

                if (CommonUtils.RA_DEBUG_debug)  System.out.println("RA("+this.ownAgentProperties.getID()+"): chacteristic size:" + robProperties.getCharacteristics().size() + "    count " + y);

                if (y != 0) {
                    dutility /= y;
                    RoleUtility rc = new RoleUtility(dutility, robProperties, this.roles.get(key));
                    this.sortedAgents.add(rc);
                    Collections.sort (this.sortedAgents, RoleUtility.compareTo());
                }
            }
        }

        if (this.sortedAgents.size() == 0) {
            ae.abort("RA("+this.ownAgentProperties.getID()+"): Could not establish a mapping between agents and roles. Please check capability definitions!");
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

                if(otherOne.getAgentProperties().getID() != thisOne.getAgentProperties().getID())
                    return otherOne.getAgentProperties().getID() < thisOne.getAgentProperties().getID() ? -1 :1;

                return 0;
            }
        });

        for (RoleUsage roleUsage : rp.getPriorityList()) {
            //if (CommonUtils.RA_DEBUG_debug) System.out.println("RA("+this.ownAgentProperties.getID()+"): role:" + roleUsage.getRole().getName() );
            for (RoleUtility agentRoleUtil : this.sortedAgents) {
                //if (CommonUtils.RA_DEBUG_debug) System.out.println("RA("+this.ownAgentProperties.getID()+"):    agentID:" + agentRoleUtil.getAgentProperties().getID());
                //if (CommonUtils.RA_DEBUG_debug) System.out.println("RA("+this.ownAgentProperties.getID()+"):    agent:" + agentRoleUtil.getAgentProperties().getName() + " " + roleUsage.getRole().getName() +"  "+ agentRoleUtil.getRole().getName());

                if (roleUsage.getRole() == agentRoleUtil.getRole()) {
                    //if (CommonUtils.RA_DEBUG_debug) System.out.println("RA("+this.ownAgentProperties.getID()+"):      role:" + roleUsage.getRole().getName() + " is equal");
                    if (this.agentRoleMapping.size() != 0
                            && (this.agentRoleMapping.get(agentRoleUtil.getAgentProperties().getID()) != null
                            || agentRoleUtil.getUtilityValue() == 0)) {
                        if (CommonUtils.RA_DEBUG_debug) System.out.println("RA("+this.ownAgentProperties.getID()+"):        continue (" + this.agentRoleMapping.size()+ "!= 0)" +" && "+ this.agentRoleMapping.get(agentRoleUtil.getAgentProperties().getID()) +"!= null" +" || "+ agentRoleUtil.getUtilityValue() + "== 0");
                        continue;
                    }
                    if (CommonUtils.RA_DEBUG_debug) System.out.println("RA("+this.ownAgentProperties.getID()+"):          put to mapping (agentID:" + agentRoleUtil.getAgentProperties().getID() +" role:" + agentRoleUtil.getRole().getName() +")");
                    this.agentRoleMapping.put(agentRoleUtil.getAgentProperties().getID(), agentRoleUtil.getRole());

                    if (this.ownAgentProperties.getID() == agentRoleUtil.getAgentProperties().getID())
                    {
                        if (CommonUtils.RA_DEBUG_debug) System.out.println("RA("+this.ownAgentProperties.getID()+"):            set to own role (agentID:" + agentRoleUtil.getAgentProperties().getID() +" role:" + agentRoleUtil.getRole().getName()+")");
                        this.ownRole = agentRoleUtil.getRole();
                    }

                    to.getAgentById(agentRoleUtil.getAgentProperties().getID()).setLastRole(agentRoleUtil.getRole());

                    break;
                }
                else
                if (CommonUtils.RA_DEBUG_debug) System.out.println("RA("+this.ownAgentProperties.getID()+"):    fail (agentID:" + agentRoleUtil.getAgentProperties().getID() +" role:" + roleUsage.getRole().getName() +")");
            }
        }
    }
}
