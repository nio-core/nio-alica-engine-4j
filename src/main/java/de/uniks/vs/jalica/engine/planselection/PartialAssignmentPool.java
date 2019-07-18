package de.uniks.vs.jalica.engine.planselection;

import de.uniks.vs.jalica.common.ExtArrayList;
import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.model.EntryPoint;
import de.uniks.vs.jalica.engine.model.Task;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by alex on 13.07.17.
 * Updated 26.6.19
 */
public class PartialAssignmentPool {


    public int curIndex;
    public Task idleTask;
    public EntryPoint idleEP;
    public ExtArrayList<PartialAssignment> pool;

    public PartialAssignmentPool(int initialSize) {
        this.pool = new ExtArrayList<>(PartialAssignment::new, initialSize);
        this.curIndex = 0;
        this.idleEP = EntryPoint.generateIdleEntryPoint();
        this.idleTask = this.idleEP.getTask();
    }

    void increaseSize() {
        this.pool.resize(this.pool.size() * 2 + 5);
    }

    public PartialAssignment getNext() {
        PartialAssignment pa = this.pool.get(this.curIndex);

        if (++this.curIndex >= this.pool.size()) {
            throw new PoolExhaustedException("Partial Assignment Pool too small at " + this.pool.size());
        }
        return pa;
    }

    void reset() {
        this.curIndex = 0;
    }

    public PartialAssignment setNext(PartialAssignment partialAssignment) {
        this.pool.add(this.curIndex, partialAssignment);
        return partialAssignment;
    }

    private class PoolExhaustedException extends RuntimeException {

        public PoolExhaustedException(String msg) {
            System.out.println(msg);
        }
    }
}
