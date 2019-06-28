package de.uniks.vs.jalica.engine.teammanagement;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.AlicaTime;
import de.uniks.vs.jalica.engine.common.SystemConfig;
import de.uniks.vs.jalica.engine.common.config.ConfigPair;
import de.uniks.vs.jalica.engine.model.AbstractPlan;
import de.uniks.vs.jalica.engine.model.DomainVariable;
import de.uniks.vs.jalica.engine.model.EntryPoint;
import de.uniks.vs.jalica.engine.views.ActiveAgentIdView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


/**
 * Updated by alex on 21.6.19.
 */
public class TeamManager {

    private AlicaTime teamTimeOut;
    private Agent localAgent;
    private AlicaEngine engine;
    private HashMap<Long, Agent> agents;
    private boolean useConfigForTeam;

    public TeamManager(AlicaEngine engine) {
        this(engine, true);
    }

    public TeamManager(AlicaEngine engine, boolean useConfigForTeam) {
        this.localAgent = null;
        this.useConfigForTeam = useConfigForTeam;
        this.engine = engine;
        this.agents = new HashMap<>();
    }

    public void init() {
        SystemConfig sc = this.engine.getSystemConfig();
        this.teamTimeOut = new AlicaTime().inMilliseconds(Long.valueOf((String) sc.get("Alica").get("Alica.TeamTimeOut")));

        if (useConfigForTeam) {
            this.readTeamFromConfig(sc);
        }
    }

    public void setTimeLastMsgReceived(long id, AlicaTime timeLastMsgReceived) {
        Agent agent = this.agents.get(id);

        if (agent != null) {
            agent.setTimeLastMsgReceived(timeLastMsgReceived);
        } else {
            // TODO alex robot properties protokoll anstoßen
            agent = new Agent(this.engine, this.teamTimeOut, id);
            agent.setTimeLastMsgReceived(timeLastMsgReceived);
            this.agents.put(id, agent);
        }
    }

    boolean isAgentActive(long agentId) {
        Agent agentEntry = this.agents.get(agentId);

        if (agentEntry != null) {
            return agentEntry.isActive();
        } else {
            return false;
        }
    }

    /**
     * Checks if an agent is ignored
     * @param agentId an essentials::AgentID identifying the agent
     */
    public boolean isAgentIgnored(long agentId) {
        Agent agentEntry = this.agents.get(agentId);

        if (agentEntry != null) {
            return agentEntry.isIgnored();
        } else {
            return false;
        }
    }

    void setAgentIgnored(long agentId, boolean ignored) {
        Agent agentEntry = this.agents.get(agentId);

        if (agentEntry != null) {
            agentEntry.setIgnored(ignored);
        }
    }

    public boolean setSuccess(long agentId, AbstractPlan plan, EntryPoint entryPoint) {
        Agent agentEntry = this.agents.get(agentId);

        if (agentEntry != null) {
            agentEntry.setSuccess(plan, entryPoint);
            return true;
        }
        return false;
    }

    boolean setSuccessMarks(long agentId, ArrayList suceededEps) {
        Agent agentEntry = this.agents.get(agentId);

        if (agentEntry != null) {
            agentEntry.setSuccessMarks(suceededEps);
            return true;
        }
        return false;
    }

    DomainVariable getDomainVariable(long agentId, String sort) {
        Agent agentEntry = this.agents.get(agentId);

        if (agentEntry != null) {
            return agentEntry.getDomainVariable(sort);
        }
        return null;
    }

    public int getTeamSize() {
        int teamSize = 0;

        for (Map.Entry<Long, Agent> agentEntry : this.agents.entrySet()) {

            if (agentEntry.getValue().isActive())
                teamSize++;
        }
        return teamSize;
    }

    public Agent getAgentByID(long agentId) {
        return this.agents.get(agentId);
    }

    private void readTeamFromConfig(SystemConfig sc) {
        String localAgentName = this.engine.getRobotName();
        Vector<String> agentNames = ((ConfigPair)this.engine.getSystemConfig().get("Globals").get("Team")).getKeys();
        Agent agent;
        boolean foundSelf = false;

        for (String agentName : agentNames) {
            int id = Integer.valueOf((String) sc.get("Globals").get("Team." + agentName + ".ID"));
            agent = new Agent(this.engine, this.teamTimeOut, this.engine.getId(id), agentName);

            if (!foundSelf && agentName.equals(localAgentName)) {
                foundSelf = true;
                this.localAgent = agent;
                this.localAgent.setLocal(true);
            } else {

                for ( Map.Entry<Long, Agent> agentEntry : this.agents.entrySet()) {

                    if ((agentEntry.getKey()) == (agent.getId()))
                        CommonUtils.aboutError("TM: Two robots with the same ID in Globals.conf. ID: " + agent.getId());
                }
            }
            this.agents.put(agent.getId(), agent);
        }
        if (!foundSelf)
            CommonUtils.aboutError("TM: Could not find own agent name in Globals Id = " + localAgentName);

        if (sc.get("Alica").get("Alica.TeamBlackList.InitiallyFull") != null) {

            for (Map.Entry<Long, Agent> agentEntry : this.agents.entrySet()) {
                agentEntry.getValue().setIgnored(true);
            }
        }
    }

    public Agent getLocalAgent() {
        return localAgent;
    }

    public void setLocalAgent(Agent localAgent) {
        this.localAgent = localAgent;
    }

    public HashMap<Long, Agent> getActiveAgents() {
        return agents;
    }

    public ActiveAgentIdView getActiveAgentIds() {
        return new ActiveAgentIdView(this.agents);
    }

    public long getLocalAgentID() {
        return this.localAgent.getId();
    }
}
