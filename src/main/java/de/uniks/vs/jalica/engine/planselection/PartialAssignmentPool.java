package de.uniks.vs.jalica.engine.planselection;

import de.uniks.vs.jalica.engine.model.EntryPoint;
import de.uniks.vs.jalica.engine.model.Task;

import java.util.Vector;

/**
 * Created by alex on 13.07.17.
 */
public class PartialAssignmentPool {


    public int curentIndex;
    public Task idleTask;
    public EntryPoint idleEntryPoint;
    public Vector<PartialAssignment> partialAssignments;
    private int initialSize;

    public PartialAssignmentPool(int initialSize) {
        this.initialSize = initialSize;

        idleEntryPoint = new EntryPoint();
        idleEntryPoint.setName("IDLE-ep");
        idleEntryPoint.setID(EntryPoint.IDLEID);
        idleEntryPoint.setMinCardinality(0);
        idleEntryPoint.setMaxCardinality(Integer.MAX_VALUE);

        idleTask = new Task(true);
        idleTask.setName("IDLE-TASK");
        idleTask.setID(Task.IDLEID);

        idleEntryPoint.setTask(idleTask);
        partialAssignments = new Vector<>();

        for (int i = 0; i < initialSize; i++) {
            partialAssignments.add( new PartialAssignment(this));
        }
    }

    public int getMaxCount() {
        return initialSize;
    }
}
