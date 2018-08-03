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
            CommonUtils.aboutCallNotification();
            thread.notifyAll();
        }
    }

    public void notifyOneThread() {
        synchronized (thread) {
            CommonUtils.aboutCallNotification();
            System.out.println("CV: notify");
            thread.notify();
        }
    }
}
