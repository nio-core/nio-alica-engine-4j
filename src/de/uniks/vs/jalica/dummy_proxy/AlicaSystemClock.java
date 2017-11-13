package de.uniks.vs.jalica.dummy_proxy;

import de.uniks.vs.jalica.supplementary.Timer;
import de.uniks.vs.jalica.unknown.AlicaTime;
import de.uniks.vs.jalica.unknown.CommonUtils;
import de.uniks.vs.jalica.unknown.IAlicaClock;

/**
 * Created by alex on 13.07.17.
 */
public class AlicaSystemClock implements IAlicaClock {

    public AlicaTime now() {
//        time_t t;
//        t = time(NULL);
        long t = Timer.getCurrentTimeInNanoSec();
//        AlicaTime ret = new AlicaTime(t * 1000000000 + t*1000*1000);
        AlicaTime ret = new AlicaTime(t);
        return ret;
    }

    public void sleep(long us){
        long sec = us/1000000;
        long nsec = (us%1000000)*1000;
        try {
            Thread.sleep(sec*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
