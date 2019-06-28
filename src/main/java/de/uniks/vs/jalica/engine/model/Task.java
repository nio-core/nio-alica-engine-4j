package de.uniks.vs.jalica.engine.model;

/**
 * Created by alex on 13.07.17.
 * Updated 26.6.19
 */
public class Task extends AlicaElement {

    public static final long IDLEID = -1;
    public static final String IDLENAME = "IDLE-TASK";

    TaskRepository taskRepository;

    public Task() {
        this.taskRepository = null;
    }

    public void setTaskRepository(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
}
