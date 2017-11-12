package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.dummy_proxy.AlicaDummyCommunication;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.teamobserver.TeamObserver;

import java.util.*;

/**
 * Created by alex on 13.07.17.
 */
public class RoleAssignment implements IRoleAssignment {

    private  HashMap<Integer, Role> robotRoleMapping;
    private  Vector<RobotRoleUtility> sortedRobots;
    private  RobotProperties ownRobotProperties;
    private  RoleSet roleSet;
    private  Role ownRole;
    private  ArrayList<RobotProperties> availableRobots;
    private  AlicaEngine ae;
    private  TeamObserver to;
    private  HashMap<Long, Role> roles;
    private  AlicaCommunication communication;
    private  boolean updateRoles;

    public RoleAssignment(AlicaEngine ae) {
        this.ae = ae;
        this.updateRoles = false;
        this.robotRoleMapping = new HashMap<Integer, Role>();
        this.sortedRobots = new Vector<RobotRoleUtility>();
    }

    public void init() {
        this.to = ae.getTeamObserver();
        //TODO delegates missing
        //to.onTeamCHangedEvent += Update;

        this.ownRobotProperties = to.getOwnRobotProperties();
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
        this.availableRobots = ae.getTeamObserver().getAvailableRobotProperties();

        System.out.println("RA: Available robots: " + this.availableRobots.size() );
        System.out.println("RA: Robot Ids: ");

        for (RobotProperties aRobot : this.availableRobots) {
            System.out.println("           " + aRobot.getId() + " " +aRobot.getName());
        }
        System.out.println();
        double dutility = 0;
        this.sortedRobots.clear();

        for ( long key : this.roles.keySet()) {

            for (RobotProperties robProperties : this.availableRobots) {
                int y = 0;
                dutility = 0;
                HashMap<String, Characteristic> characteristics = this.roles.get(key).getCharacteristics();

                for ( String roleCharacKey : characteristics.keySet()) {
                    // find the characteristics object of a robot
                    Characteristic rbChar = null;
                    String roleCharacName = characteristics.get(roleCharacKey).getName(); // roleCharacEntry.second.getName();

                    HashMap<String, Characteristic> robPropertiesCharacteristics = robProperties.getCharacteristics();

                    for ( String robotCharacKey : robPropertiesCharacteristics.keySet()) {

                        if (robotCharacKey.equals(roleCharacName)) {
                            rbChar = robPropertiesCharacteristics.get(robotCharacKey); // robotCharac.second;
                            break;
                        }
                    }

                    if (rbChar != null) {
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
                    RobotRoleUtility rc = new RobotRoleUtility(dutility, robProperties, this.roles.get(key));
                    this.sortedRobots.add(rc);
                    Collections.sort (this.sortedRobots, RobotRoleUtility.compareTo());
//                    sort(this.sortedRobots.begin(), this.sortedRobots.end(), RobotRoleUtility.compareTo);
                }
            }
        }

        if (this.sortedRobots.size() == 0) {
            ae.abort("RA: Could not establish a mapping between robots and roles. Please check capability definitions!");
        }
        RolePriority rp = new RolePriority(ae);
        this.robotRoleMapping.clear();

        while (this.robotRoleMapping.size() < this.availableRobots.size()) {
            mapRoleToRobot(rp);
        }
        rp = null;
    }

    private void mapRoleToRobot(RolePriority rp) {

        this.sortedRobots.sort(new Comparator<RobotRoleUtility>() {
            @Override
            public int compare(RobotRoleUtility thisOne, RobotRoleUtility otherOne) {
                if(otherOne.getRole().getId() != thisOne.getRole().getId())
                    return otherOne.getRole().getId() < thisOne.getRole().getId() ? -1 :1;

                if(otherOne.getUtilityValue() != thisOne.getUtilityValue())
                    return otherOne.getUtilityValue() < thisOne.getUtilityValue() ? -1 :1;

                if(otherOne.getRobot().getId() != thisOne.getRobot().getId())
                    return otherOne.getRobot().getId() < thisOne.getRobot().getId() ? -1 :1;

                return 0;
            }
        });

        for (RoleUsage roleUsage : rp.getPriorityList())
        {

            for (RobotRoleUtility robRoleUtil : this.sortedRobots)
            {

                if (roleUsage.getRole() == robRoleUtil.getRole())
                {
                    if (this.robotRoleMapping.size() != 0
                            && (this.robotRoleMapping.get(robRoleUtil.getRobot().getId()) != null
                            || robRoleUtil.getUtilityValue() == 0))
                    {
                        continue;
                    }
                    this.robotRoleMapping.put(robRoleUtil.getRobot().getId(), robRoleUtil.getRole());

                    if (this.ownRobotProperties.getId() == robRoleUtil.getRobot().getId())
                    {
                        this.ownRole = robRoleUtil.getRole();
                    }

                    to.getRobotById(robRoleUtil.getRobot().getId()).setLastRole(robRoleUtil.getRole());

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
    public Role getRole(int robotID) {
        Role role = this.robotRoleMapping.get(robotID);

        if (role != null)
        {
            return role;
        }
		else
        {
            role = this.to.getRobotById(robotID).getLastRole();
            if (role != null)
            {
                return role;
            }
            CommonUtils.aboutError( "RA: There is no role assigned for robot: " + robotID );
        }
        return null;
    }

    public void update() {
        this.updateRoles = true;
    }
}
