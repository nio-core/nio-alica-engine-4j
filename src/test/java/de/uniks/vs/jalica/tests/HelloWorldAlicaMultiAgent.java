package de.uniks.vs.jalica.tests;

import de.uniks.vs.jalica.behaviours.BehaviourCreator;
import de.uniks.vs.jalica.behaviours.ConditionCreator;
import de.uniks.vs.jalica.behaviours.ConstraintCreator;
import de.uniks.vs.jalica.unknown.AlicaSystemClock;
import de.uniks.vs.jalica.communication.AlicaZMQCommunication;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.supplementary.FileSystem;
import de.uniks.vs.jalica.supplementary.SystemConfig;
import de.uniks.vs.jalica.behaviours.UtilityFunctionCreator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HelloWorldAlicaMultiAgent {

    private BehaviourCreator bc;
    private ConditionCreator cc;
    private ConstraintCreator crc;
    private UtilityFunctionCreator uc;

    @BeforeEach
    void beforeAll() {
        // determine the path to the testsincpp config
//        String path = supplementary::FileSystem::getSelfPath();
//        int place = path.rfind("devel");
//        path = path.substr(0, place);
//        path = path + "src/alica/alica_test/src/testsincpp";
//
//        // bring up the SystemConfig with the corresponding path
//        sc = supplementary::SystemConfig::getInstance();
//        sc->setRootPath(path);
//        sc->setConfigPath(path + "/etc");

        FileSystem.PACKAGE_SRC = "src/test/java/de/uniks/vs/jalica";
        // setup the engine
        bc = new BehaviourCreator();
        cc = new ConditionCreator();
        uc = new UtilityFunctionCreator();
        crc = new ConstraintCreator();
    }

    @Test
     public void testHelloWorldMultiAgents() throws InterruptedException {
        SystemConfig sc = new SystemConfig("nio_zero", "HelloWorld/", "log/", "config/");
        AlicaEngine alicaEngine1 = new AlicaEngine();
        alicaEngine1.setIAlicaClock(new AlicaSystemClock());
        alicaEngine1.setCommunicator(new AlicaZMQCommunication(alicaEngine1));
        boolean result = alicaEngine1.init(sc, bc, cc, uc, crc, "Roleset", "TestCommunicationMaster", "roles/", true);
        Assertions.assertTrue(result);

        SystemConfig sc2 = new SystemConfig("nio_one", "HelloWorld/", "log/", "config/");
        AlicaEngine alicaEngine2 = new AlicaEngine();
        alicaEngine2.setIAlicaClock(new AlicaSystemClock());
        alicaEngine2.setCommunicator(new AlicaZMQCommunication(alicaEngine2));
        result = alicaEngine2.init(sc2, bc, cc, uc, crc, "Roleset", "TestCommunicationMaster", "roles/", true);
        Assertions.assertTrue(result);
        alicaEngine1.start();
        alicaEngine2.start();

        for (int i = 0; i < 20000; i++) {
            Thread.sleep(3300);
            alicaEngine1.stepNotify();
            Thread.sleep(3300);
            alicaEngine2.stepNotify();
//
////		if (i > 24)
////		{
////			if (ae->getPlanBase()->getDeepestNode() != nullptr)
////				cout << "AE: " << ae->getPlanBase()->getDeepestNode()->toString() << endl;
////			if (ae2->getPlanBase()->getDeepestNode() != nullptr)
////				cout << "AE2: " << ae2->getPlanBase()->getDeepestNode()->toString() << endl;
////			cout << "-------------------------" << endl;
////		}

            if (i < 10) {
//                Assertions.assertEquals(alicaEngine1.getPlanBase().getRootNode().getActiveState().getID(), 1413200842974f);
//                Assertions.assertEquals(alicaEngine2.getPlanBase().getRootNode().getActiveState().getID(), 1413200842974f);
            }
//            if (i == 10)
//            {
//                cout << "1--------- Initial State passed ---------" << endl;
//                alicaTests::TestWorldModel::getOne()->setTransitionCondition1413201227586(true);
//                alicaTests::TestWorldModel::getTwo()->setTransitionCondition1413201227586(true);
//            }
//            if (i > 11 && i < 15)
//            {
//                ASSERT_EQ(ae->getPlanBase()->getRootNode()->getActiveState()->getId(), 1413201213955);
//                ASSERT_EQ(ae2->getPlanBase()->getRootNode()->getActiveState()->getId(), 1413201213955);
//                ASSERT_EQ((*ae->getPlanBase()->getRootNode()->getChildren()->begin())->getPlan()->getName(),
//                    string("MultiAgentTestPlan"));
//                ASSERT_EQ((*ae2->getPlanBase()->getRootNode()->getChildren()->begin())->getPlan()->getName(),
//                    string("MultiAgentTestPlan"));
//            }
//            if (i == 15)
//            {
//                for (auto iter : *ae->getBehaviourPool()->getAvailableBehaviours())
//                {
//                    if (iter.second->getName() == "Attack")
//                    {
//                        ASSERT_GT(((alica::Attack*)&*iter.second)->callCounter, 5);
//                        if (((alica::Attack*)&*iter.second)->callCounter > 3)
//                        {
//                            alicaTests::TestWorldModel::getOne()->setTransitionCondition1413201052549(true);
//                            alicaTests::TestWorldModel::getTwo()->setTransitionCondition1413201052549(true);
//                            alicaTests::TestWorldModel::getOne()->setTransitionCondition1413201370590(true);
//                            alicaTests::TestWorldModel::getTwo()->setTransitionCondition1413201370590(true);
//                        }
//                    }
//                }
//                cout << "2--------- Engagement to cooperative plan passed ---------" << endl;
//            }
//            if (i == 16)
//            {
//                ASSERT_TRUE(
//                        (*ae2->getPlanBase()->getRootNode()->getChildren()->begin())->getActiveState()->getId() == 1413201030936
//                    || (*ae->getPlanBase()->getRootNode()->getChildren()->begin())->getActiveState()->getId() == 1413201030936)
//					<< endl << (*ae2->getPlanBase()->getRootNode()->getChildren()->begin())->getActiveState()->getId()
//                    << " " << (*ae->getPlanBase()->getRootNode()->getChildren()->begin())->getActiveState()->getId()
//                    << endl;
//
//                ASSERT_TRUE(
//                        (*ae2->getPlanBase()->getRootNode()->getChildren()->begin())->getActiveState()->getId() == 1413807264574
//                    || (*ae->getPlanBase()->getRootNode()->getChildren()->begin())->getActiveState()->getId() == 1413807264574)
//					<< endl << (*ae2->getPlanBase()->getRootNode()->getChildren()->begin())->getActiveState()->getId()
//                    << " " << (*ae->getPlanBase()->getRootNode()->getChildren()->begin())->getActiveState()->getId()
//                    << endl;
//                alicaTests::TestWorldModel::getOne()->setTransitionCondition1413201227586(false);
//                alicaTests::TestWorldModel::getTwo()->setTransitionCondition1413201227586(false);
//                cout << "3--------- Passed transitions in subplan passed ---------" << endl;
//            }
//            if (i >= 17 && i <= 18)
//            {
//                ASSERT_TRUE(
//                        (*ae2->getPlanBase()->getRootNode()->getChildren()->begin())->getActiveState()->getId() == 1413201030936
//                    || (*ae->getPlanBase()->getRootNode()->getChildren()->begin())->getActiveState()->getId() == 1413201030936)
//					<< "AE State: "
//                    << (*ae->getPlanBase()->getRootNode()->getChildren()->begin())->getActiveState()->getId()
//                    << " AE2 State: "
//                    << (*ae2->getPlanBase()->getRootNode()->getChildren()->begin())->getActiveState()->getId() << endl;
//                ASSERT_TRUE(
//                        (*ae2->getPlanBase()->getRootNode()->getChildren()->begin())->getActiveState()->getId() == 1413807264574
//                    || (*ae->getPlanBase()->getRootNode()->getChildren()->begin())->getActiveState()->getId() == 1413807264574)
//					<< "AE State: "
//                    << (*ae->getPlanBase()->getRootNode()->getChildren()->begin())->getActiveState()->getId() << " "
//                    << (*ae->getPlanBase()->getRootNode()->getChildren()->begin())->getActiveState()->toString() << endl
//                    << " AE2 State: "
//                    << (*ae2->getPlanBase()->getRootNode()->getChildren()->begin())->getActiveState()->getId() << " "
//                    << (*ae2->getPlanBase()->getRootNode()->getChildren()->begin())->getActiveState()->toString() << endl;
//                if(i==18) {
//                    cout << "4--------- Stayed in these state although previous transitions are not true anymore ---------"
//                            << endl;
//                    alicaTests::TestWorldModel::getOne()->setTransitionCondition1413201389955(true);
//                    alicaTests::TestWorldModel::getTwo()->setTransitionCondition1413201389955(true);
//                }
//            }
//            if (i == 19)
//            {
//                ASSERT_TRUE(
//                        ae2->getPlanBase()->getRootNode()->getActiveState()->getId() == 1413201380359
//                    && ae->getPlanBase()->getRootNode()->getActiveState()->getId() == 1413201380359)
//					<< " AE State: "
//                    << ae->getPlanBase()->getRootNode()->getActiveState()->getId()
//                    << " AE2 State: "
//                    << ae2->getPlanBase()->getRootNode()->getActiveState()->getId() << endl;
//            }
        }

//        Thread.sleep(10000);
    }

}
