package de.uniks.vs.jalica;

import de.uniks.vs.jalica.behaviours.BehaviourCreator;
import de.uniks.vs.jalica.common.SolverType;
import de.uniks.vs.jalica.conditions.ConditionCreator;
import de.uniks.vs.jalica.constraints.ConstraintCreator;
import de.uniks.vs.jalica.dummy_proxy.AlicaDummyCommunication;
import de.uniks.vs.jalica.dummy_proxy.AlicaSystemClock;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.reasoner.CGSolver;
import de.uniks.vs.jalica.utilfunctions.UtilityFunctionCreator;

/**
 * Created by alex on 10.11.17.
 */
public class Base {

    private static AlicaEngine ae;
    private static BehaviourCreator bc;
    private static ConditionCreator cc;
    private static UtilityFunctionCreator uc;
    private static ConstraintCreator crc;

    private final String roleSetName;
    private final String masterPlanName;
    private final String roleSetDir;
    private final boolean sim;

    public Base(String roleSetName, String masterPlanName, String roleSetDir, boolean sim) {

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
        ae.setCommunicator(new AlicaDummyCommunication(ae));
        ae.addSolver(SolverType.GRADIENTSOLVER.ordinal(), new CGSolver(ae));
        ae.init(bc, cc, uc, crc, roleSetName, masterPlanName, roleSetDir, false);
    }

    public void start() {
        ae.start();

        while (true)
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }
}
