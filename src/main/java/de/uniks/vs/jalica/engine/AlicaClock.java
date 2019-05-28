package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.common.TimerEvent;

/**
 * Created by alex on 13.07.17.
 */
public class AlicaClock {

    public AlicaClock() {}

    public AlicaTime now() {
        return new AlicaTime().inNanoseconds(TimerEvent.getCurrentTimeInNanoSec());
    }

    public void sleep(AlicaTime time) {
        this.sleep(time.inNanoseconds());
    }

    public void sleep(long availTime) {
        try {
            Thread.sleep(availTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
