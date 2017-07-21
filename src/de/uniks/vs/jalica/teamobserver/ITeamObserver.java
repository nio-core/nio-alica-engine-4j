package de.uniks.vs.jalica.teamobserver;

import de.uniks.vs.jalica.unknown.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by alex on 13.07.17.
 */
public interface ITeamObserver {

    public String getOwnId() ;

    void tick(RunningPlan rootNode);

    void doBroadCast(ArrayList<Long> msg);

    RobotEngineData getOwnEngineData();

    void notifyRobotLeftPlan(AbstractPlan plan);

    ArrayList<Integer>  getAvailableRobotIds();

    int successesInPlan(Plan plan);

    LinkedHashMap<Integer, SimplePlanTree> getTeamPlanTrees();

    SuccessCollection getSuccessCollection(Plan plan);

    RobotProperties getOwnRobotProperties();
}
