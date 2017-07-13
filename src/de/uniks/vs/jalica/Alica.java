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
 * Created by alex on 13.07.17.
 */
public class Alica {

    private static AlicaEngine ae;
    private static BehaviourCreator bc;
    private static ConditionCreator cc;
    private static UtilityFunctionCreator uc;
    private static ConstraintCreator crc;

    public Alica() {
        ae = new AlicaEngine();
        bc = new BehaviourCreator();
        cc = new ConditionCreator();
        uc = new UtilityFunctionCreator();
        crc = new ConstraintCreator();

        ae.setIAlicaClock(new AlicaSystemClock());
        ae.setCommunicator(new AlicaDummyCommunication(ae));
        ae.addSolver(SolverType.GRADIENTSOLVER, new CGSolver(ae));
        String roleSetName = "master";
        String masterPlanName = "test";
        String roleSetDir = ".";
        ae.init(bc, cc, uc, crc, roleSetName, masterPlanName, roleSetDir, false);  
    }

    public static void main(String... param) {
        Alica alica = new Alica();
        alica.start();
    }

    private void start() {
        ae.start();
    }


}

