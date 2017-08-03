package de.uniks.vs.jalica.dummy_proxy;

import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.unknown.AlicaEngineInfo;
import de.uniks.vs.jalica.unknown.IAlicaCommunication;

/**
 * Created by alex on 13.07.17.
 */
public class AlicaDummyCommunication implements IAlicaCommunication{
    private AlicaEngine ae;

    public AlicaDummyCommunication(AlicaEngine ae) {

        this.ae = ae;
    }

    @Override
    public void startCommunication() {

    }

    @Override
    public void tick() {

    }

    @Override
    public void sendAlicaEngineInfo(AlicaEngineInfo statusMessage) {

    }
}
