package de.uniks.vs.jalica.dummy_proxy;

import de.uniks.vs.jalica.supplementary.TimerEvent;
import de.uniks.vs.jalica.unknown.AlicaTime;
import de.uniks.vs.jalica.unknown.IAlicaClock;

/**
 * Created by alex on 13.07.17.
 */
public class AlicaSystemClock implements IAlicaClock {

    public AlicaTime now() {
//        time_t t;
//        t = time(NULL);
        long t = TimerEvent.getCurrentTimeInNanoSec();
//        AlicaTime ret = new AlicaTime(t * 1000000000 + t*1000*1000);
        AlicaTime ret = new AlicaTime(t);
        return ret;
    }

    public void sleep(long us){
//        long sec = us/1000;
//        long nsec = (us%1000000)*1000;
        long sec = us/1000l;
        try {
//            Thread.sleep(sec*1000);
            Thread.sleep(sec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
