package de.uniks.vs.jalica.teamobserver;

import de.uniks.vs.jalica.common.Logger;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.supplementary.SystemConfig;
import de.uniks.vs.jalica.unknown.RobotEngineData;
import de.uniks.vs.jalica.unknown.RobotProperties;
import de.uniks.vs.jalica.unknown.SimplePlanTree;

import java.util.*;

/**
 * Created by alex on 13.07.17.
 */
public class TeamObserver extends ITeamObserver{

    private Logger log;
    private HashMap<Integer, SimplePlanTree> simplePlanTrees;
    private RobotEngineData me;
    private AlicaEngine ae;
    private ArrayList<RobotEngineData> allOtherRobots;
    private long teamTimeOut;
    private int myId;
    private Set<Integer> ignoredRobots = new HashSet<>();
    private RobotProperties ownRobotProperties;
    private ArrayList<RobotProperties> availableRobotProperties;

    public TeamObserver(AlicaEngine ae) {
        this.teamTimeOut = 0;
        this.myId = 0;
        this.simplePlanTrees = new HashMap<Integer, SimplePlanTree>( new HashMap<Integer, SimplePlanTree >());
        this.me = null;
        this.log = null;
        this.ae = ae;
        this.allOtherRobots = new ArrayList<RobotEngineData>();
    }

    public void init() {
        SystemConfig sc = SystemConfig.getInstance();
        this.log = ae.getLog();

        String ownPlayerName = ae.getRobotName();
        System.out.println( "TO: Initing Robot " + ownPlayerName );
        this.teamTimeOut = Long.valueOf(sc.get("Alica").get("Alica.TeamTimeOut")) * 1000000;
        Vector<String> playerNames = sc.getG("Globals").getSections("Globals.Team");
        boolean foundSelf = false;

        for (int i = 0; i < playerNames.size(); i++)
        {
            RobotProperties rp = new RobotProperties(ae, playerNames.get(i));
            if (!foundSelf && !playerNames.get(i).equals(ownPlayerName))
            {
                foundSelf = true;
                this.me = new RobotEngineData(ae, rp);
                this.me.setActive(true);
                this.myId = rp.getId();
            }
            else
            {
                for (RobotEngineData red : this.allOtherRobots)
                {
                    if (red.getProperties().getId() == rp.getId())
                    {
                        String ss;
                        ss = "TO: Found twice Robot ID " + rp.getId() + "in globals team section" + "\n";
                        ae.abort(ss);
                    }
                    if (rp.getId() == myId)
                    {
                        String ss2;
                        ss2 = "TO: Found myself twice Robot ID " + rp.getId() + "in globals team section" + "\n";
                        ae.abort(ss2);
                    }
                }
                this.allOtherRobots.add(new RobotEngineData(ae, rp));
            }
        }
        if (!foundSelf)
        {
            ae.abort("TO: Could not find own robot name in Globals Id = " + ownPlayerName);
        }

        if (Boolean.valueOf(sc.get("Alica").get("Alica.TeamBlackList.InitiallyFull")))
        {
            for (RobotEngineData r : this.allOtherRobots)
            {
                this.ignoredRobots.add(r.getProperties().getId());
            }
        }
    }

    public RobotProperties getOwnRobotProperties() {
        return ownRobotProperties;
    }

    public ArrayList<RobotProperties> getAvailableRobotProperties() {
        return availableRobotProperties;
    }
}

