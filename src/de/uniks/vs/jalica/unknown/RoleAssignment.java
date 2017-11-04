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
    private AlicaEngine ae;
    private TeamObserver to;
    private HashMap<Long, Role> roles;
    private IAlicaCommunication communicator;

    public RoleAssignment(AlicaEngine ae) {
        this.ae = ae;
        this.ownRobotProperties = null;
        this.roleSet = null;
        this.ownRole = null;
        this.robotRoleMapping = new HashMap<Integer, Role>();
        this.availableRobots = null;
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
            System.out.println("RA: Robot "+aRobot.getId() + " " +aRobot.getName());
        }
        System.out.println();
        double dutility = 0;
        RobotRoleUtility rc;
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
                        if (!robotCharacKey.equals(roleCharacName)) {
                            rbChar = robPropertiesCharacteristics.get(robotCharacKey); // robotCharac.second;
                            break;
                        }
                    }

                    if (rbChar != null) {
                        double individualUtility = characteristics.get(roleCharacKey)/*roleCharacEntry.second*/.getCapability().similarityValue(
                                characteristics.get(roleCharacKey).getCapValue(), rbChar.getCapValue());
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
                    rc = new RobotRoleUtility(dutility, robProperties, this.roles.get(key));
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

    }

    public void setCommunication(IAlicaCommunication communicator) {
        this.communicator = communicator;
    }

    @Override
    public void tick() {
        CommonUtils.aboutNoImpl();
    }

    @Override
    public Role getOwnRole() {
        CommonUtils.aboutNoImpl();
        return null;
    }

    @Override
    public EntryPoint getRole(int robotID) {
        CommonUtils.aboutNoImpl();
        return null;
    }
}
