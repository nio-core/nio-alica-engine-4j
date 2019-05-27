package de.uniks.vs.jalica.supplementary;

import de.uniks.vs.jalica.engine.common.CommonUtils;
import de.uniks.vs.jalica.engine.common.ConditionVariable;

/**
 * Created by alex on 17.07.17.
 */
public class TimerEvent extends Trigger implements Runnable {

    private String parent;
    //    private static Instant clock = Instant.now();
    protected Thread runThread;
    protected long msInterval; /** < The time between two fired events */
    protected boolean started;
    protected long msDelayedStart; /** < The time between starting the TimerEvent and the first fired event */
    protected boolean running;
    protected boolean triggered;
    protected ConditionVariable cv;
    private boolean notifyAll;

    public TimerEvent(long msInterval, long msDelayedStart) {
        this.started = false;
        this.running = false;
        this.triggered = false;
        this.msInterval  = msInterval;
        this.msDelayedStart = msDelayedStart;
        this.runThread = new Thread(this);
        this.notifyAll = false;
        this.cv = new ConditionVariable(runThread);
//        this.clock = Instant.now();
    }

    public TimerEvent(long msInterval, long msDelayedStart, String parent) {
        this(msInterval, msDelayedStart);
        this.parent = parent;
        if (CommonUtils.TE_DEBUG_debug) System.out.println("TE:  new Timer for "+ parent);
    }

    public boolean start() {

        if(!this.started) {
            if (CommonUtils.TE_DEBUG_debug) CommonUtils.aboutCallNotification();
            this.started = true;
            runThread.start();
            if (CommonUtils.TE_DEBUG_debug) CommonUtils.aboutCallNotification("TimerEvent: Thread id " + runThread.getId() + " started");
        }

        if (this.started && !this.running) {
            this.running = true;
            this.cv.notifyOneThread();
            if (CommonUtils.TE_DEBUG_debug) CommonUtils.aboutCallNotification();
        }
        return this.started && this.running;
    }

    public boolean stop() {

        if (this.started && this.running){
            this.running = false;
            this.started = false;
        }
        return this.started && this.running;
    }

    private void testMe(){

    }

    @Override
    public void run() {
        if (CommonUtils.TE_DEBUG_debug) CommonUtils.aboutCallNotification();

        if (msDelayedStart > 0) {
            try {
                Thread.sleep(msDelayedStart);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
//        unique_lock<mutex> lck(cv_mtx);

        while (this.started) {
            try {
                Thread cv = new Thread() {

                    @Override
                    public void run() {

                        while (true) {

                            if(!started || (running && registeredCVs.size() > 0)) {
                                if (CommonUtils.TE_DEBUG_debug) CommonUtils.aboutCallNotification("Timer notified");

                                synchronized (TimerEvent.this) {
                                    TimerEvent.this.notify();
                                }
                                return;
                            }
                        }
                    }
                };
                cv.start();

                synchronized (this) {
                    this.wait();
                }
                if (CommonUtils.TE_DEBUG_debug && false) System.out.println("TimerEvent:  awakened " + parent);
                if (CommonUtils.TE_DEBUG_debug && false) CommonUtils.aboutCallNotification();

//            this.cv.wait(lck, [&] {
//                    return !this.started || (this.running && this.registeredCVs.size() > 0);
//            });

//                this.cv.wait();
//                synchronized (this) {
//                    this.wait();
//                }

                if (!this.started) // for destroying the timer
                    return;

                long start = getCurrentTimeInNanoSec();
                if (CommonUtils.TE_DEBUG_debug && false) CommonUtils.aboutCallNotification("TE: notify all !!!!!");
                this.notifyAll(notifyAll);
//                this.notify();
//                this.notifyAll();
                long dura = (getCurrentTimeInNanoSec()) - start;
                if (CommonUtils.TE_DEBUG_debug) System.out.println("TE 1: Run duration is " + dura + " nanoseconds");

                if (msInterval > dura) {
                    if (CommonUtils.TE_DEBUG_debug)  System.out.println("TE 2: Wait duration is " + (msInterval - dura) + " nanoseconds");
                    Thread.sleep(Math.abs(msInterval - dura));
                }
                if (CommonUtils.TE_DEBUG_debug) System.out.println("TE 3: Total duration is " + ((getCurrentTimeInNanoSec()) - start) + " nanoseconds");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static long getCurrentTimeInNanoSec() {
        long nanoTime = System.nanoTime();
//        return clock.getEpochSecond() * 1000000000l;
        return nanoTime;
    }

    public void setDelayedStart(long msDelayedStart) {
        this.msDelayedStart = msDelayedStart;
    }

    public long getDelayedStart() {
        return this.msDelayedStart;
    }
}
