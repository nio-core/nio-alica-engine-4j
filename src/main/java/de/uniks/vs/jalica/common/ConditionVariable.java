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
    private Lock lck;
    private Runnable thread;
    private boolean interrupted;
    private Condition condition;

    public ConditionVariable(Runnable thread) {
        this.thread = thread;
        this.interrupted = false;
    }

    public void notifyAllThreads() {
        synchronized (thread) {
            if (CommonUtils.CV_DEBUG_debug) CommonUtils.aboutCallNotification("CV: notify all threads");
            thread.notifyAll();
            if (lck != null) {
                lck.lock();
                condition.signalAll();
                lck.unlock();
            }
        }
    }

    public void notifyOneThread() {
        synchronized (thread) {
            if (CommonUtils.CV_DEBUG_debug) CommonUtils.aboutCallNotification("CV: notify one thread");
            thread.notify();
            if (lck != null) {
                lck.lock();
                condition.signal();
                lck.unlock();
            }
        }
    }

    public void cvWait(Lock lck, Runnable runnable, CVCondition cvCondition) {
        this.lck = lck;
        this.thread = runnable;
        condition = lck.newCondition();
        lck.lock();
        try {
//            synchronized (thread) {
//                while (!cvCondition.isSatisfied())
//                    thread.wait();
//            }

                while (!cvCondition.isSatisfied()) {
                    if (CommonUtils.CV_DEBUG_debug) System.out.println("CV: " +cvCondition.isSatisfied());
                    condition.await();
                }
        } catch (InterruptedException e) {
            this.interrupted = true;
        } finally {
            if (CommonUtils.CV_DEBUG_debug) System.out.println("CV: notified");
            lck.unlock();
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
