package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.supplementary.TimerEvent;

public class AlicaClock extends IAlicaClock {

    public AlicaClock() {}

    @Override
    public AlicaTime now() {
        return new AlicaTime().inNanoseconds(TimerEvent.getCurrentTimeInNanoSec());
    }

    @Override
    public void sleep(AlicaTime time) {
        this.sleep(time.inNanoseconds());
    }
}
