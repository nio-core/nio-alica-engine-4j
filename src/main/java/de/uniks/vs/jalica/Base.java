package de.uniks.vs.jalica;

import de.uniks.vs.jalica.behaviours.BehaviourCreator;
import de.uniks.vs.jalica.common.SolverType;
import de.uniks.vs.jalica.behaviours.ConditionCreator;
import de.uniks.vs.jalica.behaviours.ConstraintCreator;
import de.uniks.vs.jalica.dummy_proxy.AlicaSystemClock;
import de.uniks.vs.jalica.dummy_proxy.AlicaZMQCommunication;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.reasoner.CGSolver;
import de.uniks.vs.jalica.supplementary.SystemConfig;
import de.uniks.vs.jalica.behaviours.UtilityFunctionCreator;

/**
 * Created by alex on 10.11.17.
 */
public class Base {

    private static AlicaEngine ae;
    private static BehaviourCreator bc;
    private static ConditionCreator cc;
    private static UtilityFunctionCreator uc;
    private static ConstraintCreator crc;

    private String roleSetName;
    private String masterPlanName;
    private String roleSetDir;
    private boolean sim;
    private String id;

    public Base(String roleSetName, String masterPlanName, String roleSetDir, boolean sim) {
        init(roleSetName, masterPlanName, roleSetDir, sim);
    }

    public Base(String id, String roleSetName, String masterPlanName, String roleSetDir, boolean sim) {
        this.id = id;
        init(roleSetName,  masterPlanName, roleSetDir, sim);
    }

    private void init(String roleSetName, String masterPlanName, String roleSetDir, boolean sim) {
        this.roleSetName = roleSetName;
        this.masterPlanName = masterPlanName;
        this.roleSetDir = roleSetDir;
        this.sim = sim;

        ae = new AlicaEngine();
        bc = new BehaviourCreator();
        cc = new ConditionCreator();
        uc = new UtilityFunctionCreator();
        crc = new ConstraintCreator();

        ae.setIAlicaClock(new AlicaSystemClock());
        ae.addSolver(SolverType.GRADIENTSOLVER.ordinal(), new CGSolver(ae));
        SystemConfig sc = new SystemConfig(this.id);
        ae.setCommunicator(new AlicaZMQCommunication(ae));
        ae.init(sc, bc, cc, uc, crc, roleSetName, masterPlanName, roleSetDir, false);
    }

    public void start() {
        ae.start();

        //TODO: Refactoring
        while (true)
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }
}
