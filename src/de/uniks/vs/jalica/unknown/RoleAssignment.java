package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.dummy_proxy.AlicaDummyCommunication;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.teamobserver.TeamObserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by alex on 13.07.17.
 */
public class RoleAssignment extends IRoleAssignment{

    private  HashMap<Integer, Role> robotRoleMapping;
    private  Vector<RobotRoleUtility> sortedRobots;
    private  RobotProperties ownRobotProperties;
    private  RoleSet roleSet;
    private  Role ownRole;
    private  ArrayList<RobotProperties> availableRobots;
    private AlicaEngine ae;
    private TeamObserver to;
    private HashMap<Long, Role> roles;

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
        if (this.roleSet == null)
        {
            cerr << "RA: The current Roleset is null!" << endl;
            throw new exception();
        }
        this.availableRobots = ae.getTeamObserver().getAvailableRobotProperties();

        cout << "RA: Available robots: " << this.availableRobots.size() << endl;
        cout << "RA: Robot Ids: ";
        for (auto aRobot : (this.availableRobots))
        {
            cout << aRobot.getId() << " ";
        }
        cout << endl;
        double dutility = 0;
        RobotRoleUtility rc;
        this.sortedRobots.clear();
        for ( long key : this.roles.keySet())
        {
            for (auto robProperties : (this.availableRobots))
            {
                int y = 0;
                dutility = 0;
                for ( roleCharacEntry : this.roles.get(key).getCharacteristics())
                {
                    // find the characteristics object of a robot
                    Characteristic rbChar = null;
                    String roleCharacName = roleCharacEntry.second.getName();
                    for ( robotCharac : robProperties.getCharacteristics())
                    {
                        if (robotCharac.first.compare(roleCharacName) == 0)
                        {
                            rbChar = robotCharac.second;
                            break;
                        }
                    }

                    if (rbChar != null)
                    {
                        double individualUtility = roleCharacEntry.second.getCapability().similarityValue(
                            roleCharacEntry.second.getCapValue(), rbChar.getCapValue());
                        if (individualUtility == 0)
                        {
                            dutility = 0;
                            break;
                        }
                        dutility += (roleCharacEntry.second.getWeight() * individualUtility);
                        y++;
                    }
                }
                if (y != 0)
                {
                    dutility /= y;
                    rc = new RobotRoleUtility(dutility, robProperties, this.roles.get(key));
                    this.sortedRobots.add(rc);
                    sort(this.sortedRobots.begin(), this.sortedRobots.end(), RobotRoleUtility::compareTo);
                }
            }
        }
        if (this.sortedRobots.size() == 0)
        {
            ae.abort("RA: Could not establish a mapping between robots and roles. Please check capability definitions!");
        }
        RolePriority rp = new RolePriority(ae);
        this.robotRoleMapping.clear();
        while (this.robotRoleMapping.size() < this.availableRobots.size())
        {
            mapRoleToRobot(rp);
        }
        rp = null;
    }

    private void mapRoleToRobot(RolePriority rp) {

    }

    public void setCommunication(AlicaDummyCommunication communicator) {

    }
}
