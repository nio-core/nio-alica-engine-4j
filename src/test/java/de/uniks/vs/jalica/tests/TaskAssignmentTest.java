package de.uniks.vs.jalica.tests;

import de.uniks.vs.jalica.autogenerated.BehaviourCreator;
import de.uniks.vs.jalica.behaviours.ConditionCreator;
import de.uniks.vs.jalica.behaviours.ConstraintCreator;
import de.uniks.vs.jalica.communication.AlicaZMQCommunication;
import de.uniks.vs.jalica.engine.AlicaClock;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.RunningPlan;
import de.uniks.vs.jalica.engine.IPlanSelector;
import de.uniks.vs.jalica.common.FileSystem;
import de.uniks.vs.jalica.engine.common.SystemConfig;
import de.uniks.vs.jalica.behaviours.UtilityFunctionCreator;
import de.uniks.vs.jalica.engine.idmanagement.ID;
import de.uniks.vs.jalica.engine.idmanagement.IDManager;
import de.uniks.vs.jalica.engine.model.AbstractPlan;
import de.uniks.vs.jalica.engine.model.Plan;
import de.uniks.vs.jalica.engine.model.Role;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class TaskAssignmentTest {

    private BehaviourCreator bc;
    private ConditionCreator cc;
    private ConstraintCreator crc;
    private UtilityFunctionCreator uc;

    private AlicaEngine alicaEngine;

    @BeforeEach
    void beforeAll() {
        bc = new BehaviourCreator();
        cc = new ConditionCreator();
        uc = new UtilityFunctionCreator();
        crc = new ConstraintCreator();

        FileSystem.PACKAGE_SRC = "src/test/java/de/uniks/vs/jalica";

        SystemConfig sc = new SystemConfig("nase");
        alicaEngine = new AlicaEngine(new IDManager(), "RolesetTA", sc, "MasterPlanTaskAssignment", false);
        alicaEngine.setAlicaClock(new AlicaClock());
        alicaEngine.setCommunicator(new AlicaZMQCommunication(alicaEngine));
        boolean result = alicaEngine.init(bc, cc, uc, crc);
        Assertions.assertTrue(result);
    }

    @Test
     public void testMultiAgents() throws InterruptedException {
        Vector<Long> agents = new Vector<>();

        for (long number = 8; number <= 11; number++) {
            ID agentID = alicaEngine.getId(number);
            agents.add(agentID.asLong());
            alicaEngine.getTeamManager().setTimeLastMsgReceived(agentID, alicaEngine.getAlicaClock().now());
        }
        // fake inform the team observer about roles of none existing agents
        alicaEngine.getTeamObserver().tick(null);
        alicaEngine.getRoleAssignment().tick();

        HashMap<Long, Plan> planMap = alicaEngine.getPlanRepository().getPlans();
        RunningPlan rp = new RunningPlan(alicaEngine, planMap.get(1407152758497l));
        ArrayList<AbstractPlan> inputPlans = new ArrayList<>();
        inputPlans.add((planMap.get(1407152758497l)));
//        IPlanSelector ps = alicaEngine.getPlanSelector();
//
//        ArrayList<RunningPlan> o_plans = ps.getPlansForState(rp, inputPlans, agents);
//        Assert.assertNotNull(o_plans);
//        Assert.assertEquals (1, o_plans);

//        HashMap<Long, Role> roles = alicaEngine.getPlanRepository().getRoles();
//        int i = 8;
//
//        for (Role role : roles.values()) {
//            alicaEngine.getTeamObserver().getAgentById(i).setCurrentRole(role);
//            i++;
//            if (i > 11)
//                break;
//        }
//
//        HashMap<Long, Plan> planMap = alicaEngine.getPlanRepository().getPlans();
//        RunningPlan rp = new RunningPlan(alicaEngine, planMap.get(1407152758497l));
//        ArrayList<AbstractPlan> planList = new ArrayList<>();
//        planList.add((planMap.get(1407152758497l)));
//        IPlanSelector ps = alicaEngine.getPlanSelector();
//        ArrayList<RunningPlan> plans = ps.getPlansForState(rp, planList, agents);

//        alicaEngine.start();
        Thread.sleep(2000);
    }

    @AfterAll
    void AfterAll() {
        alicaEngine.shutdown();
    }

}

