package de.uniks.vs.jalica.engine.model;

import java.util.ArrayList;

/**
 * Created by alex on 13.07.17.
 */
public class TaskRepository extends AlicaElement {
    private long id;
    private String fileName;
    private long defaultTask;
    ArrayList<Task> tasks = new ArrayList<>();

    public void setID(long id) {
        this.id = id;
    }

    public long getID() {
        return id;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setDefaultTask(long defaultTask) {
        this.defaultTask = defaultTask;
    }

    public long getDefaultTask() {
        return defaultTask;
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }
}
