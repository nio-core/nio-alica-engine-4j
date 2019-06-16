package de.uniks.vs.jalica.engine.taskassignment;

/**
 * Created by alex on 31.07.17.
 */
public class TaskRole {
    public long taskId;
    public long roleId;


    public TaskRole(long taskId, long roleId) {
        this.taskId = taskId;
        this.roleId = roleId;
    }
}
