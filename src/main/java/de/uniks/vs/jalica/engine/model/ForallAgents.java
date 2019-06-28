package de.uniks.vs.jalica.engine.model;

import de.uniks.vs.jalica.engine.RunningPlan;
import de.uniks.vs.jalica.engine.collections.AgentEngineData;
import de.uniks.vs.jalica.engine.collections.AgentVariables;
import de.uniks.vs.jalica.engine.teammanagement.TeamManager;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by alex on 03.08.17.
 */
public class ForallAgents extends Quantifier {

    private enum Result {
        ADDED,
        MODIFIED,
        NONE
    };

    public ForallAgents() {
        super();
    }

    private Result tryAddId(long id, ArrayList<AgentVariables> io_agentVarsInScope, TeamManager teamManager ) {
        AgentEngineData robotEngineData = teamManager.getAgentByID(id).getEngineData();
        AgentVariables agentVariables = null;

        for ( AgentVariables variables : io_agentVarsInScope ) {
            if (variables.getId() == id)
                agentVariables = variables;
        }

        if (agentVariables == null) {
            // add new agent
            AgentVariables newAgent = new AgentVariables(id);
            newAgent.getVars().ensureCapacity(getDomainIdentifiers().size());

            for (String s : getDomainIdentifiers()) {
                newAgent.getVars().add(robotEngineData.getDomainVariable(s));
            }

            io_agentVarsInScope.add(newAgent);
            return Result.ADDED;
        } else {
            // modify existing agent
            AgentVariables oldAgent = agentVariables;
            Result result = Result.NONE;

            for (String identifier : getDomainIdentifiers()) {
                boolean notFound = true;

                for (Object variable : oldAgent.getVars()) {

                    if (((DomainVariable)variable).getName() == identifier)
                        notFound = false;
                }
                if (notFound){
                    oldAgent.getVars().add(robotEngineData.getDomainVariable(identifier));
                    result = Result.MODIFIED;
                }
            }
            return result;
        }
    }

    @Override
    public boolean isAgentInScope(long id,  RunningPlan runningPlan) {

        switch (getScopeType()) {
            case PLANSCOPE:
                return runningPlan.getActivePlan() == getScopedPlan() && runningPlan.getAssignment().hasAgent(id);
            case ENTRYPOINTSCOPE:
                return runningPlan.getAssignment().getEntryPointOfAgent(id) == getScopedEntryPoint();
            case STATESCOPE:
                return runningPlan.getAssignment().getStateOfAgent(id) == getScopedState();
        }
        assert(false);
        return false;
    }

    @Override
    public boolean addDomainVariables( RunningPlan p, ArrayList<AgentVariables> ioAgentVarsInScope) {
        boolean addedAgent = false;
        boolean changedAgent = false;
        TeamManager teamManager = p.getAlicaEngine().getTeamManager();

        switch (getScopeType()) {
            case PLANSCOPE:
                if (p.getActivePlan() == getScopedPlan()) {
                    for (Long id : p.getAssignment().getAllAgents().get()) {
                        Result result = tryAddId(id, ioAgentVarsInScope, teamManager);
                        addedAgent = addedAgent || result == Result.ADDED;
                        changedAgent = changedAgent || result == Result.MODIFIED;
                    }
                }
                break;
            case ENTRYPOINTSCOPE:
                if (p.getActivePlan() == getScopedEntryPoint().getPlan()) {
                for (Long id : p.getAssignment().getAgentsWorking(getScopedEntryPoint()).get()) {
                    Result result = tryAddId(id, ioAgentVarsInScope, teamManager);
                    addedAgent = addedAgent || result == Result.ADDED;
                    changedAgent = changedAgent || result == Result.MODIFIED;
                }
            }
            break;

            case STATESCOPE:
                if (p.getActivePlan() == getScopedState().getInPlan()) {
                for (Long id : p.getAssignment().getAgentsInState(getScopedState()).get()) {
                    Result result = tryAddId(id, ioAgentVarsInScope, teamManager);
                    addedAgent = addedAgent || result == Result.ADDED;
                    changedAgent = changedAgent || result == Result.MODIFIED;
                }
            }
            break;
        }

        if (addedAgent) {
            ioAgentVarsInScope.sort( Comparator.comparingDouble(value -> value.getId()));
        }
        return addedAgent || changedAgent;
    }
}
