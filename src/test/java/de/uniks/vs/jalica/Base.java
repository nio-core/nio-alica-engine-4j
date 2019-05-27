package de.uniks.vs.jalica;

import de.uniks.vs.jalica.behaviours.BehaviourCreator;
import de.uniks.vs.jalica.behaviours.ConditionCreator;
import de.uniks.vs.jalica.behaviours.UtilityFunctionCreator;
import de.uniks.vs.jalica.behaviours.ConstraintCreator;
import de.uniks.vs.jalica.engine.IBehaviourCreator;
import de.uniks.vs.jalica.engine.IConditionCreator;
import de.uniks.vs.jalica.engine.IConstraintCreator;
import de.uniks.vs.jalica.engine.IUtilityFunctionCreator;
import de.uniks.vs.jalica.engine.common.SolverType;
import de.uniks.vs.jalica.communication.AlicaZMQCommunication;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.reasoner.CGSolver;
import de.uniks.vs.jalica.supplementary.SystemConfig;
import de.uniks.vs.jalica.engine.AlicaClock;

/**
 * Created by alex on 10.11.17.
 */
public class Base {

    private static AlicaEngine ae;
    private static IBehaviourCreator bc;
    private static IConditionCreator cc;
    private static IUtilityFunctionCreator uc;
    private static IConstraintCreator crc;

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

        ae.setIAlicaClock(new AlicaClock());
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
