package de.uniks.vs.jalica.unknown;

/**
 * Created by alex on 14.07.17.
 */

@Deprecated
public class ConditionVariable {
    private Runnable thread;

    public ConditionVariable(Runnable thread) {
        this.thread = thread;
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
}
