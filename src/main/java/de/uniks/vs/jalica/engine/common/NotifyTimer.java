package de.uniks.vs.jalica.engine.common;

import de.uniks.vs.jalica.engine.constrainmodule.VariableSyncModule;

public class NotifyTimer<V> extends Thread {

    private final long interval;
    private final VariableSyncModule variableSyncModule;

    boolean running;
    boolean started;

    public NotifyTimer(long interval, VariableSyncModule variableSyncModule) {
        this.interval = interval;
        this.variableSyncModule = variableSyncModule;
        this.started = true;
        this.running = false;
    }

    @Override
    public void run() {

        while (this.started){
            long start = System.currentTimeMillis();

            if (this.running){
                this.variableSyncModule.publishContent();
            }
            long dura = System.currentTimeMillis() - start;

            try {
                Thread.sleep(interval - dura >0 ?interval - dura: 0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void start() {
        startIt();
        super.start();
    }
    public boolean startIt() {

        if (this.started && !this.running){
            this.running = true;
        }
        return this.started && this.running;
    }

    public boolean stopIt() {

        if (this.started && this.running) {
            this.running = false;
        }
        return this.started && this.running;
    }
}
