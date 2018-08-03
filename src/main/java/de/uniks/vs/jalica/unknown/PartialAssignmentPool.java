package de.uniks.vs.jalica.unknown;

import java.util.Vector;

/**
 * Created by alex on 13.07.17.
 */
public class PartialAssignmentPool {

    public static int maxCount = 10100;

    public int curentIndex;
    public Task idleTask;
    public EntryPoint idleEntryPoint;
    public Vector<PartialAssignment> partialAssignments;

    public PartialAssignmentPool() {
        // IDLE-EntryPoint
        idleEntryPoint = new EntryPoint();
        idleEntryPoint.setName("IDLE-ep");
        idleEntryPoint.setID(EntryPoint.IDLEID);
        idleEntryPoint.setMinCardinality(0);
//        idleEntryPoint.setMaxCardinality(numeric_limits<int>::max());
        idleEntryPoint.setMaxCardinality(Integer.MAX_VALUE);

        // Add IDLE-Task
        idleTask = new Task(true);
        idleTask.setName("IDLE-TASK");
        idleTask.setID(Task.IDLEID);

        idleEntryPoint.setTask(idleTask);
        partialAssignments = new Vector<>();

        for (int i = 0; i < maxCount; i++) {
            partialAssignments.add( new PartialAssignment(this));
        }
    }

}
