package de.uniks.vs.jalica.common;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.BasicBehaviour;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Created by alex on 14.07.17.
 */

@Deprecated
public class ConditionVariable {
    private Runnable thread;
    private boolean interrupted;

    public ConditionVariable(Runnable thread) {
        this.thread = thread;
        this.interrupted = false;
    }

    public void notifyAllThreads() {
        synchronized (thread) {
            if (CommonUtils.CV_DEBUG_debug) CommonUtils.aboutCallNotification("CV: notify all threads");
            thread.notifyAll();
        }
    }

    public void notifyOneThread() {
        synchronized (thread) {
            if (CommonUtils.CV_DEBUG_debug) CommonUtils.aboutCallNotification("CV: notify one thread");
            thread.notify();
        }
    }

    public void cvWait(Lock lck, Runnable runnable, CVCondition cvCondition) {
        this.thread = runnable;
        Condition condition = lck.newCondition();
        try {
            while (cvCondition.isSatisfied())
            condition.await();
        } catch (InterruptedException e) {
            this.interrupted = true;
        }
    }

    public boolean cvWaitFor(Lock lck, long timeInNanoSeconds) {
        Condition condition = lck.newCondition();
        try {
            condition.awaitNanos(timeInNanoSeconds);
        } catch (InterruptedException e) {
            this.interrupted = true;
        }
        return true;
    }

    public boolean isInterrupted() {
        return interrupted;
    }
}
