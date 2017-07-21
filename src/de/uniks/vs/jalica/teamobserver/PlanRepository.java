package de.uniks.vs.jalica.teamobserver;


import de.uniks.vs.jalica.unknown.*;
import de.uniks.vs.jalica.behaviours.Behaviour;
import de.uniks.vs.jalica.behaviours.BehaviourConfiguration;

import java.util.HashMap;

/**
 **   Created by alex on 13.07.17.
 **/
public class PlanRepository {

    HashMap<Long, Plan> plans;
    HashMap<Long, Task> tasks;
    HashMap<Long, Behaviour> behaviours;
    HashMap<Long, BehaviourConfiguration> behaviourConfigurations = new HashMap<>();
    HashMap<Long, PlanType> planTypes;
    HashMap<Long, Role> roles = new HashMap();
    HashMap<Long, Characteristic> characteristics;
    HashMap<Long, Capability> capabilities;
    HashMap<Long, State> states;
    HashMap<Long, EntryPoint> entryPoints;
    HashMap<Long, Transition > transitions;
    HashMap<Long, SyncTransition > syncTransitions;
    HashMap<Long, Quantifier > quantifiers;
    HashMap<Long, Variable> variables;
    HashMap<Long, RoleDefinitionSet > roleDefinitionSets;
    HashMap<Long, TaskRepository > taskRepositorys;
    HashMap<Long, PlanningProblem > planningProblems;

    public HashMap<Long, BehaviourConfiguration> getBehaviourConfigurations() {
        return behaviourConfigurations;
    }

    public HashMap<Long, Role> getRoles() {
        return roles;
    }
}
