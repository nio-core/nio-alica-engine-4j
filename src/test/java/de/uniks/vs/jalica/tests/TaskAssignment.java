package de.uniks.vs.jalica.tests;

import de.uniks.vs.jalica.autogenerated.BehaviourCreator;
import de.uniks.vs.jalica.behaviours.ConditionCreator;
import de.uniks.vs.jalica.behaviours.ConstraintCreator;
import de.uniks.vs.jalica.communication.AlicaZMQCommunication;
import de.uniks.vs.jalica.engine.AlicaClock;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.RunningPlan;
import de.uniks.vs.jalica.engine.planselection.IPlanSelector;
import de.uniks.vs.jalica.supplementary.FileSystem;
import de.uniks.vs.jalica.supplementary.SystemConfig;
import de.uniks.vs.jalica.behaviours.UtilityFunctionCreator;
import de.uniks.vs.jalica.engine.model.AbstractPlan;
import de.uniks.vs.jalica.engine.model.Plan;
import de.uniks.vs.jalica.engine.model.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class TaskAssignment {

    private BehaviourCreator bc;
    private ConditionCreator cc;
    private ConstraintCreator crc;
    private UtilityFunctionCreator uc;

    @BeforeEach
    void beforeAll() {
//// determine the path to the testsincpp config
//        string path = supplementary::FileSystem::getSelfPath();
//        int place = path.rfind("devel");
//        path = path.substr(0, place);
//        path = path + "src/alica/alica_test/src/testsincpp";
//
//        // bring up the SystemConfig with the corresponding path
//        sc = supplementary::SystemConfig::getInstance();
//        sc->setRootPath(path);
//        sc->setConfigPath(path + "/etc");
//        cout << sc->getConfigPath() << endl;
//
//        sc->setHostname("nase");
//        ae = new alica::AlicaEngine();
//        bc = new alica::BehaviourCreator();
//        cc = new alica::ConditionCreator();
//        uc = new alica::UtilityFunctionCreator();
//        crc = new alica::ConstraintCreator();
//        ae->setIAlicaClock(new alicaRosProxy::AlicaROSClock());
//        ae->init(bc, cc, uc, crc, "RolesetTA", "MasterPlanTaskAssignment", ".", false);

        bc = new BehaviourCreator();
        cc = new ConditionCreator();
        uc = new UtilityFunctionCreator();
        crc = new ConstraintCreator();
    }

    @Test
     public void testMulitAgents() throws InterruptedException {

        FileSystem.PACKAGE_SRC = "src/test/java/de/uniks/vs/jalica";

        SystemConfig sc = new SystemConfig("nase");
        AlicaEngine alicaEngine = new AlicaEngine();
        alicaEngine.setIAlicaClock(new AlicaClock());
        alicaEngine.setCommunicator(new AlicaZMQCommunication(alicaEngine));
        boolean result = alicaEngine.init(sc, bc, cc, uc, crc, "RolesetTA", "MasterPlanTaskAssignment", "roles/", false);
        Assertions.assertTrue(result);


        Vector<Long> robots = new Vector<>();

        for (long i = 8; i <= 11; i++) {
            robots.add(i);
        }

        HashMap<Long, Role> roles = alicaEngine.getPlanRepository().getRoles();
        int i = 8;

        for (Role role : roles.values()) {
            alicaEngine.getTeamObserver().getAgentById(i).setLastRole(role);
            i++;
            if (i > 11)
                break;
        }

        HashMap<Long, Plan> planMap = alicaEngine.getPlanRepository().getPlans();
        RunningPlan rp = new RunningPlan(alicaEngine, planMap.get(1407152758497l));
        ArrayList<AbstractPlan> planList = new ArrayList<>();
        planList.add((planMap.get(1407152758497l)));
        IPlanSelector ps = alicaEngine.getPlanSelector();
        ArrayList<RunningPlan> plans = ps.getPlansForState(rp, planList, robots);

//        alicaEngine.start();
        Thread.sleep(2000);
    }

}
