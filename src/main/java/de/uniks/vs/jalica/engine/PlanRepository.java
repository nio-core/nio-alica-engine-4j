package de.uniks.vs.jalica.engine;


import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.common.Capability;
import de.uniks.vs.jalica.engine.common.PlanningProblem;
import de.uniks.vs.jalica.engine.common.SyncTransition;
import de.uniks.vs.jalica.engine.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * *   Created by alex on 13.07.17.
 * updated 23.6.19
 **/
public class PlanRepository {

    //TODO: refactoring (struct)

    LinkedHashMap<Long, Plan> plans;
    LinkedHashMap<Long, Task> tasks;
    LinkedHashMap<Long, Behaviour> behaviours;
    LinkedHashMap<Long, PlanType> planTypes;
    LinkedHashMap<Long, Role> roles;
    LinkedHashMap<Long, Characteristic> characteristics;
    LinkedHashMap<Long, Capability> capabilities;
    LinkedHashMap<Long, State> states;
    LinkedHashMap<Long, EntryPoint> entryPoints;
    LinkedHashMap<Long, Transition> transitions;
    LinkedHashMap<Long, Quantifier> quantifiers;
    LinkedHashMap<Long, Variable> variables;
    LinkedHashMap<Long, PlanningProblem> planningProblems;
    LinkedHashMap<Long, Condition> conditions;
    LinkedHashMap<Long, Synchronisation> synchronisations;
    LinkedHashMap<Long, RoleSet> roleSets;
    LinkedHashMap<Long, TaskRepository> taskRepositories;

    public PlanRepository() {
        this.plans = new LinkedHashMap<>();
        this.tasks = new LinkedHashMap<>();
        this.behaviours = new LinkedHashMap<>();
        this.planTypes = new LinkedHashMap<>();
        this.roles = new LinkedHashMap<>();
        this.characteristics = new LinkedHashMap<>();
        this.capabilities = new LinkedHashMap<>();
        this.states = new LinkedHashMap<>();
        this.entryPoints = new LinkedHashMap<>();
        this.transitions = new LinkedHashMap<>();
        this.conditions = new LinkedHashMap<>();
        this.synchronisations = new LinkedHashMap<>();
        this.quantifiers = new LinkedHashMap<>();
        this.variables = new LinkedHashMap<>();
        this.roleSets = new LinkedHashMap<>();
        this.taskRepositories = new LinkedHashMap<>();
        this.planningProblems = new LinkedHashMap<>();
    }


    boolean checkVarsInCondition(Condition c, Plan p) {
        if (c == null) {
            return true;
        }
        ArrayList<Variable> pvars = p.getVariables();
        for (Variable v : c.getVariables()) {
            if (!pvars.contains(v)) {
                System.err.println("Variable " + v.toString() + " used in Condition " + c.toString() + " in Plan " + p.toString()
                        + " is not properly contained in the plan.");
                assert (false);
                return false;
            }
        }

        return true;
    }

    boolean checkVarsInVariableBindings(Plan p) {
        ArrayList<Variable> pvars = p.getVariables();
        for (State s : p.getStates()) {
            for (VariableBinding pr : s.getParametrisation()) {
                if (!pvars.contains(pr.getVar())) {
                    System.err.println("Variable " + pr.getVar().toString() + " used in Parametrisation of state " + s.toString() + " in Plan " + p.toString()
                            + " is not properly contained in the plan.");
                    assert (false);
                    return false;
                }
            }
        }
        return true;
    }

    boolean checkVarsInPlan(Plan p) {
        boolean ret = checkVarsInCondition(p.getPreCondition(), p);
        ret = ret && checkVarsInCondition(p.getRuntimeCondition(), p);
        ret = ret && checkVarsInVariableBindings(p);

        return ret;
    }

    boolean verifyPlanBase() {
        // Every entrypoint has a task:
        for (EntryPoint ep : getEntryPoints().values()) {
            if (ep.getTask() == null) {
                System.err.println("EntryPoint " + ep.toString() + " does not have a task.");
                assert (false);
                return false;
            }
        }
        // Every plans entryPoints are sorted:
        for (Plan p : getPlans().values()) {
            for (int i = 0; i < p.getEntryPoints().size() - 1; ++i) {

                if (p.getEntryPoints().get(i).getID() >= p.getEntryPoints().get(i + 1).getID()) {
                    System.err.println("Wrong sorting of entrypoints in plan " + p.toString());
                    assert (false);
                    return false;
                }
            }
            checkVarsInPlan(p);
        }

        return true;
    }

    public LinkedHashMap<Long, Plan> getPlans() {
        return plans;
    }

    public LinkedHashMap<Long, Task> getTasks() {
        return tasks;
    }

    public LinkedHashMap<Long, Behaviour> getBehaviours() {
        return behaviours;
    }

    public LinkedHashMap<Long, PlanType> getPlanTypes() {
        return planTypes;
    }

    public LinkedHashMap<Long, Role> getRoles() {
        return roles;
    }

    public LinkedHashMap<Long, Characteristic> getCharacteristics() {
        return characteristics;
    }

    public LinkedHashMap<Long, Capability> getCapabilities() {
        return capabilities;
    }

    public LinkedHashMap<Long, State> getStates() {
        return states;
    }

    public LinkedHashMap<Long, EntryPoint> getEntryPoints() {
        return entryPoints;
    }

    public LinkedHashMap<Long, Transition> getTransitions() {
        return transitions;
    }

    public LinkedHashMap<Long, Quantifier> getQuantifiers() {
        return quantifiers;
    }

    public LinkedHashMap<Long, Variable> getVariables() {
        return variables;
    }

    public LinkedHashMap<Long, PlanningProblem> getPlanningProblems() {
        return planningProblems;
    }

    public LinkedHashMap<Long, Condition> getConditions() {
        return conditions;
    }

    public LinkedHashMap<Long, Synchronisation> getSynchronisations() {
        return synchronisations;
    }

    public LinkedHashMap<Long, RoleSet> getRoleSets() {
        return roleSets;
    }

    public LinkedHashMap<Long, TaskRepository> getTaskRepositories() {
        return taskRepositories;
    }


    //    public boolean verifyPlanBase() {
//        // Every entrypoint has a task:
//        for ( EntryPoint ep : getEntryPoints().values()) {
//
//            if (ep.getTask() == null) {
//                CommonUtils.aboutError("EntryPoint " + ep.toString() + " does not have a task.");
//                return false;
//            }
//        }
//        // Every plans entryPoints are sorted:
//        for ( Plan p : getPlans().values()) {
//        for (int i = 0; i < p.getEntryPoints().size() - 1; i ++) {
//            if (p.getEntryPoints().get(i).getID() >= p.getEntryPoints().get(i + 1).getID()) {
//                CommonUtils.aboutError("Wrong sorting of entrypoints in plan " + p.toString() );
//                return false;
//            }
//        }
//        checkVarsInPlan(p);
//    }
//        return true;
//    }
//
//    private boolean checkVarsInPlan(Plan p) {
//        boolean ret = checkVarsInCondition(p.getPreCondition(), p);
//        ret = ret && checkVarsInCondition(p.getRuntimeCondition(), p);
//        ret = ret && checkVarsInVariableBindings(p);
//
//        return ret;
//    }
//
//    private boolean checkVarsInCondition(Condition c, Plan p) {
//        if (c == null)
//            return true;
//        ArrayList<Variable> pvars = p.getVariables();
//
//        for (Variable v : c.getVariables()) {
//            if (!pvars.contains(v)) {
//                CommonUtils.aboutError("Variable " + v.toString() + " used in Condition " + c.toString() + " in Plan " + p.toString()
//                        + " is not properly contained in the plan.");
//                return false;
//            }
//        }
//        return true;
//    }
//
//    private boolean checkVarsInVariableBindings(Plan p) {
//        ArrayList<Variable> pvars = p.getVariables();
//
//        for (State s : p.getStates()) {
//
//            for (VariableBinding pr : s.getParametrisation()) {
//                if (!pvars.contains(pr)) {
//                    CommonUtils.aboutError("Variable " + pr.getVar().toString() + " used in Parametrisation of state " + s.toString() + " in Plan " + p.toString()
//                            + " is not properly contained in the plan.");
//                    return false;
//                }
//            }
//        }
//        return true;
//    }
}
