package de.uniks.vs.jalica.supplementary;

import de.uniks.vs.jalica.unknown.CommonUtils;
import de.uniks.vs.jalica.unknown.condition_variable;

import java.time.Instant;

/**
 * Created by alex on 17.07.17.
 */
public class Timer extends ITrigger implements Runnable {

    private static Instant clock = Instant.now();
    protected Thread runThread;
    protected long msInterval; /** < The time between two fired events */
    protected long msDelayedStart; /** < The time between starting the TimerEvent and the first fired event */
    protected boolean running;
    protected boolean started;
    protected boolean triggered;
    protected condition_variable cv;

    public Timer(long msInterval, long msDelayedStart) {
        this.started = true;
        this.running = false;
        this.triggered = false;
        this.msInterval = msInterval;
        this.msDelayedStart = msDelayedStart;
        this.runThread = new Thread(this);
//        this.clock = Instant.now();
    }

    public boolean start() {
        return false;
    }

    public boolean stop() {
        return false;
    }

    @Override
    void run(boolean notifyAll) {
        CommonUtils.aboutNoImpl();
    }

    @Override
    public boolean isNotifyCalled(Object p0) {
        CommonUtils.aboutNoImpl();
        return false;
    }

    @Override
    public void setNotifyCalled(boolean called, Object p1) {
        CommonUtils.aboutNoImpl();
    }

    @Override
    public void run() {

        if (msDelayedStart > 0) {

            try {
                Thread.sleep(msDelayedStart);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

//        unique_lock<mutex> lck(cv_mtx);

        while (this.started) {
//            this.cv.wait(lck, [&] {
//                    return !this.started || (this.running && this.registeredCVs.size() > 0);
//            });
            try {
                this.cv.wait();

                if (!this.started) // for destroying the timer
                    return;

                long start = getCurrentTimeInNanoSec();
//                this.notifyAll(notifyAll);
                this.notifyAll();
                long dura = (getCurrentTimeInNanoSec()) - start;
                System.out.println("T: Duration is " + dura + " nanoseconds");
                Thread.sleep(msInterval - dura);
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
}
