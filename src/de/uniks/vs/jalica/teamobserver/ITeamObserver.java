package de.uniks.vs.jalica.teamobserver;

import de.uniks.vs.jalica.unknown.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by alex on 13.07.17.
 */
public interface ITeamObserver {

    public int getOwnID() ;

    void tick(RunningPlan rootNode);

    void doBroadCast(ArrayList<Long> msg);

    RobotEngineData getOwnEngineData();

    void notifyAgentLeftPlan(AbstractPlan plan);

    ArrayList<Integer> getAvailableAgentIDs();

    int successesInPlan(Plan plan);

    LinkedHashMap<Integer, SimplePlanTree> getTeamPlanTrees();

    SuccessCollection getSuccessCollection(Plan plan);

    AgentProperties getOwnAgentProperties();

    int teamSize();

    void handlePlanTreeInfo(PlanTreeInfo pti);

    void updateSuccessCollection(Plan plan, SuccessCollection epSuccessMapping);
}
