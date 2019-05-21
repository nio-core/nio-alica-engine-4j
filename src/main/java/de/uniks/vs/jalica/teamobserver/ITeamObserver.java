package de.uniks.vs.jalica.teamobserver;

import de.uniks.vs.jalica.unknown.*;
import de.uniks.vs.jalica.unknown.Communication.PlanTreeInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by alex on 13.07.17.
 */
public interface ITeamObserver {

    public long getOwnID() ;

    void tick(RunningPlan rootNode);

    void doBroadCast(ArrayList<Long> msg);

    AgentEngineData getOwnEngineData();

    void notifyAgentLeftPlan(AbstractPlan plan);

    ArrayList<Long> getAvailableAgentIDs();

    int successesInPlan(Plan plan);

    LinkedHashMap<Long, SimplePlanTree> getTeamPlanTrees();

    SuccessCollection getSuccessCollection(Plan plan);

    AgentProperties getOwnAgentProperties();

    int teamSize();

    void handlePlanTreeInfo(PlanTreeInfo pti);

    void updateSuccessCollection(Plan plan, SuccessCollection epSuccessMapping);
}
