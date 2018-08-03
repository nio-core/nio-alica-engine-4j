package de.uniks.vs.jalica.tests;

import de.uniks.vs.jalica.autogenerated.BehaviourCreator;
import de.uniks.vs.jalica.behaviours.ConditionCreator;
import de.uniks.vs.jalica.behaviours.ConstraintCreator;
import de.uniks.vs.jalica.behaviours.DummyTestSummand;
import de.uniks.vs.jalica.common.USummand;
import de.uniks.vs.jalica.dummy_proxy.AlicaSystemClock;
import de.uniks.vs.jalica.dummy_proxy.AlicaZMQCommunication;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.supplementary.FileSystem;
import de.uniks.vs.jalica.supplementary.SystemConfig;
import de.uniks.vs.jalica.behaviours.UtilityFunctionCreator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Authority {

    private BehaviourCreator bc;
    private ConditionCreator cc;
    private ConstraintCreator crc;
    private UtilityFunctionCreator uc;

    @BeforeEach
    void beforeAll() {

//        // determine the path to the testsincpp config
//        string path = supplementary::FileSystem::getSelfPath();
//        int place = path.rfind("devel");
//        path = path.substr(0, place);
//        path = path + "src/alica/alica_test/src/testsincpp";
//
//        // bring up the SystemConfig with the corresponding path
//        sc = supplementary::SystemConfig::getInstance();
//        sc->setRootPath(path);
//        sc->setConfigPath(path + "/etc");
//        sc->setHostname("nase");
//
//        // setup the engine
//        ae = new alica::AlicaEngine();
//        bc = new alica::BehaviourCreator();
//        cc = new alica::ConditionCreator();
//        uc = new alica::UtilityFunctionCreator();
//        crc = new alica::ConstraintCreator();
//        ae->setIAlicaClock(new alicaRosProxy::AlicaROSClock());
//        ae->setCommunicator(new alicaRosProxy::AlicaRosCommunication(ae));

        bc = new BehaviourCreator();
        cc = new ConditionCreator();
        uc = new UtilityFunctionCreator();
        crc = new ConstraintCreator();
    }

    @Test
     public void testMulitAgents() throws InterruptedException {

        FileSystem.PACKAGE = "src/test/java/de/uniks/vs/jalica";

        SystemConfig sc = new SystemConfig("nase");
        AlicaEngine alicaEngine1 = new AlicaEngine();
        alicaEngine1.setIAlicaClock(new AlicaSystemClock());
        alicaEngine1.setCommunicator(new AlicaZMQCommunication(alicaEngine1));
        boolean result1 = alicaEngine1.init(sc, bc, cc, uc, crc, "RolesetTA", "AuthorityTestMaster", "roles/", false);
        Assertions.assertTrue(result1);

//        sc = new SystemConfig("hairy");
//        AlicaEngine alicaEngine2 = new AlicaEngine();
//        alicaEngine2.setIAlicaClock(new AlicaSystemClock());
//        alicaEngine2.setCommunicator(new AlicaZMQCommunication(alicaEngine2));
//        boolean result2 = alicaEngine2.init(sc, bc, cc, uc, crc, "RolesetTA", "AuthorityTestMaster", "roles/", false);
//        Assertions.assertTrue(result2);


        USummand uSummandAe = ((alicaEngine1.getPlanRepository().getPlans().get(1414403413451l)).getUtilityFunction().getUtilSummands().get(0));
        DummyTestSummand dbr = (DummyTestSummand)(uSummandAe);
        dbr.setAgentID(alicaEngine1.getTeamObserver().getOwnID());
//        USummand uSummandAe2 = ((alicaEngine2.getPlanRepository().getPlans().get(1414403413451l)).getUtilityFunction().getUtilSummands().get(0));
//        DummyTestSummand dbr2 = (DummyTestSummand)(uSummandAe2);
//        dbr2.setAgentID(alicaEngine2.getTeamObserver().getOwnID());
        alicaEngine1.start();
//        alicaEngine2.start();

//        alicaTests::TestWorldModel::getOne().robotsXPos.push_back(0);
//        alicaTests::TestWorldModel::getOne().robotsXPos.push_back(2000);
//
//        alicaTests::TestWorldModel::getTwo().robotsXPos.push_back(2000);
//        alicaTests::TestWorldModel::getTwo().robotsXPos.push_back(0);
//
//        for (int i = 0; i < 21; i++)
//        {
//            ae.stepNotify();
//            chrono::milliseconds duration(33);
//            this_thread::sleep_for(duration);
//            ae2.stepNotify();
//            this_thread::sleep_for(duration);
//            if (i == 1)
//            {
//                EXPECT_EQ((*ae.getPlanBase().getRootNode().getChildren().begin()).getActiveState().getId(), 1414403553717);
//                EXPECT_EQ((*ae2.getPlanBase().getRootNode().getChildren().begin()).getActiveState().getId(), 1414403553717);
//            }
//
//            if (i == 20)
//            {
//                EXPECT_EQ((*ae.getPlanBase().getRootNode().getChildren().begin()).getActiveState().getId(), 1414403553717);
//                EXPECT_EQ((*ae2.getPlanBase().getRootNode().getChildren().begin()).getActiveState().getId(), 1414403429950);
//            }
//        }

        Thread.sleep(2000000);
    }

}
