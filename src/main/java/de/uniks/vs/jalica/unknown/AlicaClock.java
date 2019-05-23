package de.uniks.vs.jalica.unknown;

/**
 * Created by alex on 13.07.17.
 */
public interface AlicaClock {

    AlicaTime now();

    void sleep(long availTime);
}
