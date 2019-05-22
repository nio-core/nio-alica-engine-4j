package de.uniks.vs.jalica.unknown;

/**
 * Created by alex on 13.07.17.
 */
public abstract class IAlicaClock {

    public abstract AlicaTime now();

    public abstract void sleep(AlicaTime availTime);

    public void sleep(long availTime) {
        try {
            Thread.sleep(availTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
