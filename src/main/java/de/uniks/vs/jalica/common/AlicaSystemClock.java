package de.uniks.vs.jalica.common;

import de.uniks.vs.jalica.engine.AlicaTime;
import de.uniks.vs.jalica.engine.AlicaClock;

/**
 * Created by alex on 13.07.17.
 */
@Deprecated
public class AlicaSystemClock extends AlicaClock {

    public AlicaTime now() {
        long t = TimerEvent.getCurrentTimeInNanoSec();
        AlicaTime ret = new AlicaTime().inNanoseconds(t);
        return ret;
    }
}
