package de.uniks.vs.jalica.common;

/**
 * Created by alex on 31.07.17.
 */
public class TaskRoleStruct {
    public long taskId;
    public long roleId;


    public TaskRoleStruct(long taskId, long roleId) {
        this.taskId = taskId;
        this.roleId = roleId;
    }
}
