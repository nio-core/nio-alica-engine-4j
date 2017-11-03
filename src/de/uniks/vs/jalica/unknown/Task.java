package de.uniks.vs.jalica.unknown;

/**
 * Created by alex on 13.07.17.
 */
public class Task extends AlicaElement {

    String description;
    TaskRepository taskRepository;
    boolean defaultTask;


    public Task(boolean defaultTask) {
        super();
        this.defaultTask = defaultTask;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTaskRepository(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
}
