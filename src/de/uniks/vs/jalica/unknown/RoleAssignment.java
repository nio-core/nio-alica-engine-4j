package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.teamobserver.TeamObserver;

import java.util.*;

/**
 * Created by alex on 13.07.17.
 */
public class RoleAssignment implements IRoleAssignment {

    private  HashMap<Integer, Role> agentRoleMapping;
    private  Vector<RoleUtility> sortedAgents;
    private AgentProperties ownAgentProperties;
    private  RoleSet roleSet;
    private  Role ownRole;
    private  ArrayList<AgentProperties> availableAgents;
    private  AlicaEngine ae;
    private  TeamObserver to;
    private  HashMap<Long, Role> roles;
    private  AlicaCommunication communication;
    private  boolean updateRoles;

    public RoleAssignment(AlicaEngine ae) {
        this.ae = ae;
        this.updateRoles = false;
        this.agentRoleMapping = new HashMap<Integer, Role>();
        this.sortedAgents = new Vector<RoleUtility>();
    }

    public void init() {
        this.to = ae.getTeamObserver();
        //TODO delegates missing
        //to.onTeamCHangedEvent += Update;

        this.ownAgentProperties = to.getOwnAgentProperties();
        roleUtilities();
    }

    private void roleUtilities() {
        this.roleSet = ae.getRoleSet();
        this.roles = ae.getPlanRepository().getRoles();

        if (this.roleSet == null) {
            System.err.println( "RA: The current Roleset is null!" );
            try {
                throw new Exception();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.availableAgents = ae.getTeamObserver().getAvailableAgentProperties();

        System.out.println("RA: Available agents: " + this.availableAgents.size() );
        System.out.println("RA: agent Ids: ");

        for (AgentProperties agentProperties : this.availableAgents) {
            System.out.println("           " + agentProperties.getID() + " " +agentProperties.getName());
        }
        System.out.println();
        double dutility = 0;
        this.sortedAgents.clear();

        for ( long key : this.roles.keySet()) {

            for (AgentProperties robProperties : this.availableAgents) {
                int y = 0;
                dutility = 0;
                HashMap<String, Characteristic> characteristics = this.roles.get(key).getCharacteristics();

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
//                        Characteristic characteristic = characteristics.get(roleCharacKey);
//                        Capability capability = characteristic.getCapability();
//                        Characteristic characteristic1 = characteristics.get(roleCharacKey);
//                        CapValue characteristic1CapValue = characteristic1.getCapValue();
//                        CapValue charCapValue = rbChar.getCapValue();
//                        double v = capability.similarityValue(characteristic1CapValue, charCapValue);

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

                if (y != 0) {
                    dutility /= y;
                    RoleUtility rc = new RoleUtility(dutility, robProperties, this.roles.get(key));
                    this.sortedAgents.add(rc);
                    Collections.sort (this.sortedAgents, RoleUtility.compareTo());
//                    sort(this.sortedAgents.begin(), this.sortedAgents.end(), RoleUtility.compareTo);
                }
            }
        }

        if (this.sortedAgents.size() == 0) {
            ae.abort("RA: Could not establish a mapping between agents and roles. Please check capability definitions!");
        }
        RolePriority rp = new RolePriority(ae);
        this.agentRoleMapping.clear();

        while (this.agentRoleMapping.size() < this.availableAgents.size()) {
            mapRoleToAgent(rp);
        }
        rp = null;
    }

    private void mapRoleToAgent(RolePriority rp) {

        this.sortedAgents.sort(new Comparator<RoleUtility>() {
            @Override
            public int compare(RoleUtility thisOne, RoleUtility otherOne) {
                if(otherOne.getRole().getId() != thisOne.getRole().getId())
                    return otherOne.getRole().getId() < thisOne.getRole().getId() ? -1 :1;

                if(otherOne.getUtilityValue() != thisOne.getUtilityValue())
                    return otherOne.getUtilityValue() < thisOne.getUtilityValue() ? -1 :1;

                if(otherOne.getAgentProperties().getID() != thisOne.getAgentProperties().getID())
                    return otherOne.getAgentProperties().getID() < thisOne.getAgentProperties().getID() ? -1 :1;

                return 0;
            }
        });

        for (RoleUsage roleUsage : rp.getPriorityList()) {

            for (RoleUtility agentRoleUtil : this.sortedAgents) {

                if (roleUsage.getRole() == agentRoleUtil.getRole())
                {
                    if (this.agentRoleMapping.size() != 0
                            && (this.agentRoleMapping.get(agentRoleUtil.getAgentProperties().getID()) != null
                            || agentRoleUtil.getUtilityValue() == 0))
                    {
                        continue;
                    }
                    this.agentRoleMapping.put(agentRoleUtil.getAgentProperties().getID(), agentRoleUtil.getRole());

                    if (this.ownAgentProperties.getID() == agentRoleUtil.getAgentProperties().getID())
                    {
                        this.ownRole = agentRoleUtil.getRole();
                    }

                    to.getAgentById(agentRoleUtil.getAgentProperties().getID()).setLastRole(agentRoleUtil.getRole());

                    break;
                }
            }
        }
    }

    public void setCommunication(AlicaCommunication communication) {
        this.communication = communication;
    }

    @Override
    public void tick() {

        if (this.updateRoles) {
            this.updateRoles = false;
            this.roleUtilities();
        }
    }

    @Override
    public Role getOwnRole() {
        return ownRole;
    }

    public void setOwnRole(Role ownRole)
    {
        if (this.ownRole != ownRole)
        {
            RoleSwitch rs = new RoleSwitch();
            rs.roleID = ownRole.getId();
            this.communication.sendRoleSwitch(rs);
        }
        this.ownRole = ownRole;
    }
    
    @Override
    public Role getRole(int agentID) {
        Role role = this.agentRoleMapping.get(agentID);

        if (role != null)
        {
            return role;
        }
		else
        {
            role = this.to.getAgentById(agentID).getLastRole();
            if (role != null)
            {
                return role;
            }
            CommonUtils.aboutError( "RA: There is no role assigned for agent: " + agentID);
        }
        return null;
    }

    public void update() {
        this.updateRoles = true;
    }
}
