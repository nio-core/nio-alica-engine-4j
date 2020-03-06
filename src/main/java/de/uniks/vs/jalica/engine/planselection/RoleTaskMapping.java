package de.uniks.vs.jalica.engine.planselection;

import de.uniks.vs.jalica.engine.model.AlicaElement;
import de.uniks.vs.jalica.engine.model.Role;

import java.util.HashMap;

/**
 * Created by alex on 03.11.17.
 */
public class RoleTaskMapping extends AlicaElement {
    private Role role;
    private HashMap<Long, Double> taskPriorities;

    public RoleTaskMapping() {
        taskPriorities = new HashMap<>();
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public HashMap<Long, Double> getTaskPriorities() {
        return taskPriorities;
    }

    public void setTaskPriorities(HashMap<Long, Double> taskPriorities) {
        this.taskPriorities = taskPriorities;
    }
}