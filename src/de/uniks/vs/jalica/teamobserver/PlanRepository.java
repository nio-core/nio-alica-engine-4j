package de.uniks.vs.jalica.teamobserver;


import de.uniks.vs.jalica.unknown.*;
import de.uniks.vs.jalica.behaviours.Behaviour;
import de.uniks.vs.jalica.behaviours.BehaviourConfiguration;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 **   Created by alex on 13.07.17.
 **/
public class PlanRepository {

    LinkedHashMap<Long, Plan> plans = new LinkedHashMap<>();
    LinkedHashMap<Long, Task> tasks = new LinkedHashMap<>();
    LinkedHashMap<Long, Behaviour> behaviours = new LinkedHashMap<>();
    LinkedHashMap<Long, BehaviourConfiguration> behaviourConfigurations = new LinkedHashMap<>();
    LinkedHashMap<Long, PlanType> planTypes = new LinkedHashMap<>();
    LinkedHashMap<Long, Role> roles = new LinkedHashMap();
    LinkedHashMap<Long, Characteristic> characteristics;
    LinkedHashMap<Long, Capability> capabilities;
    LinkedHashMap<Long, State> states  = new LinkedHashMap<>();
    LinkedHashMap<Long, EntryPoint> entryPoints  = new LinkedHashMap<>();
    LinkedHashMap<Long, Transition > transitions  = new LinkedHashMap<>();
    LinkedHashMap<Long, SyncTransition > syncTransitions = new LinkedHashMap<>();
    LinkedHashMap<Long, Quantifier > quantifiers;
    LinkedHashMap<Long, Variable> variables = new LinkedHashMap<>();
    LinkedHashMap<Long, RoleDefinitionSet > roleDefinitionSets;
    LinkedHashMap<Long, TaskRepository > taskRepositorys;
    LinkedHashMap<Long, PlanningProblem > planningProblems;

    public HashMap<Long, BehaviourConfiguration> getBehaviourConfigurations() {
        return behaviourConfigurations;
    }

    public HashMap<Long, Role> getRoles() {
        return roles;
    }

    public HashMap<Long, EntryPoint> getEntryPoints() {
        return entryPoints;
    }

    public HashMap<Long,Plan> getPlans() {
        return plans;
    }

    public LinkedHashMap<Long, State> getStates() {
        return states;
    }

    public LinkedHashMap<Long,Transition> getTransitions() {
        return transitions;
    }

    public LinkedHashMap<Long, Quantifier> getQuantifiers() {
        return quantifiers;
    }

    public LinkedHashMap<Long,Variable> getVariables() {
        return variables;
    }

    public LinkedHashMap<Long,SyncTransition> getSyncTransitions() {
        return syncTransitions;
    }
}
