package de.uniks.vs.jalica.unknown;

/**
 * Created by alex on 13.07.17.
 */
public interface  IAlicaCommunication {

    public void startCommunication();

    void tick();

    void sendAlicaEngineInfo(AlicaEngineInfo statusMessage);
}
