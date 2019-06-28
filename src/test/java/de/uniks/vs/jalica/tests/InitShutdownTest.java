package de.uniks.vs.jalica.tests;

import de.uniks.vs.jalica.common.FileSystem;
import de.uniks.vs.jalica.communication.dummy_proxy.AlicaDummyCommunication;
import de.uniks.vs.jalica.engine.*;
import de.uniks.vs.jalica.engine.common.SystemConfig;
import de.uniks.vs.jalica.engine.idmanagement.IDManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;

public class InitShutdownTest {

    private AlicaEngine alicaEngine;
    private SystemConfig systemConfig;

    private IBehaviourCreator behaviourCreator;
    private IConditionCreator conditionCreator;
    private IUtilityCreator utilityFunctionCreator;
    private IConstraintCreator constraintCreator;

    @Before
    public void setup() {
        FileSystem.PACKAGE_SRC = "src/java/de/uniks/vs/jalica/tests";
        FileSystem.PACKAGE_ROOT = Paths.get(".").toAbsolutePath().toString();

        String root = "generated/";
        String log = "log/";
        String config =  root + "/config/";

        systemConfig = new SystemConfig("nase", root, log, config);

        alicaEngine = new AlicaEngine(new IDManager(), "RoleSet", systemConfig, "MasterPlan", false);

//        bc = new BehaviourCreator();
//        cc = new ConditionCreator();
//        uc = new UtilityFunctionCreator();
//        crc = new ConstraintCreator();
        alicaEngine.setAlicaClock(new AlicaClock());
        alicaEngine.setCommunicator(new AlicaDummyCommunication(alicaEngine));
    }

    @Test
    public void test() {
        Assert.assertTrue("Unable teamObserver initialise the Alica Engine!", alicaEngine.init( behaviourCreator, conditionCreator, utilityFunctionCreator, constraintCreator));
    }

    @After
    public void teardown() {

//        alicaEngine.shutdown();
//        systemConfig.shutdown();
    }

}
