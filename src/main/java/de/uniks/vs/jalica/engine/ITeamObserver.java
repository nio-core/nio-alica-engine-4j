package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.engine.RunningPlan;
import de.uniks.vs.jalica.engine.SimplePlanTree;
import de.uniks.vs.jalica.engine.containers.messages.PlanTreeInfo;
import de.uniks.vs.jalica.engine.model.AbstractPlan;
import de.uniks.vs.jalica.engine.model.Plan;
import de.uniks.vs.jalica.engine.collections.AgentEngineData;
import de.uniks.vs.jalica.engine.collections.AgentProperties;
import de.uniks.vs.jalica.engine.collections.SuccessCollection;

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
