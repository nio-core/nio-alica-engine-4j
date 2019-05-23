package de.uniks.vs.jalica.dummy_proxy;

import de.uniks.vs.jalica.supplementary.TimerEvent;
import de.uniks.vs.jalica.unknown.AlicaClock;
import de.uniks.vs.jalica.unknown.AlicaTime;

/**
 * Created by alex on 13.07.17.
 */
public class AlicaSystemClock extends AlicaClock {

    public AlicaTime now() {
        long t = TimerEvent.getCurrentTimeInNanoSec();
        AlicaTime ret = new AlicaTime().inNanoseconds(t);
        return ret;
    }
}
