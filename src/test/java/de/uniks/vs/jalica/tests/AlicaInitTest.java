package de.uniks.vs.jalica.tests;

import de.uniks.vs.jalica.autogenerated.BehaviourCreator;
import de.uniks.vs.jalica.behaviours.ConditionCreator;
import de.uniks.vs.jalica.behaviours.ConstraintCreator;
import de.uniks.vs.jalica.common.AlicaSystemClock;
import de.uniks.vs.jalica.communication.AlicaZMQCommunication;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.common.FileSystem;
import de.uniks.vs.jalica.engine.common.SystemConfig;
import de.uniks.vs.jalica.behaviours.UtilityFunctionCreator;
import de.uniks.vs.jalica.engine.idmanagement.IDManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AlicaInitTest {

    private BehaviourCreator bc;
    private ConditionCreator cc;
    private ConstraintCreator crc;
    private UtilityFunctionCreator uc;

    @BeforeEach
    void beforeAll() {
        bc = new BehaviourCreator();
        cc = new ConditionCreator();
        uc = new UtilityFunctionCreator();
        crc = new ConstraintCreator();
    }

    @Test
     public void testAlicaInit() throws InterruptedException {

        FileSystem.PACKAGE_SRC = "src/test/java/de/uniks/vs/jalica";

        SystemConfig sc = new SystemConfig("nase");
        AlicaEngine alicaEngine = new AlicaEngine(new IDManager(),"Roleset", sc, "MasterPlan", false);
        alicaEngine.setAlicaClock(new AlicaSystemClock());
        alicaEngine.setCommunicator(new AlicaZMQCommunication(alicaEngine));
        boolean result = alicaEngine.init(bc, cc, uc, crc);

        if(!result)
            System.out.println("Unable teamObserver initialise the Alica Engine!");
        Assertions.assertTrue(result);

        Thread.sleep(2000);
    }

}