package de.uniks.vs.jalica;

import de.uniks.vs.jalica.behaviours.BehaviourCreator;
import de.uniks.vs.jalica.behaviours.ConditionCreator;
import de.uniks.vs.jalica.behaviours.ConstraintCreator;
import de.uniks.vs.jalica.behaviours.UtilityFunctionCreator;
import de.uniks.vs.jalica.communication.AlicaZMQCommunication;
import de.uniks.vs.jalica.engine.*;
import de.uniks.vs.jalica.engine.common.SolverType;
import de.uniks.vs.jalica.engine.common.SystemConfig;
import de.uniks.vs.jalica.engine.idmanagement.IDManager;

/**
 * Created by alex on 10.11.17.
 */
public class Base {

    private static AlicaEngine ae;
    private static IBehaviourCreator bc;
    private static IConditionCreator cc;
    private static IUtilityCreator uc;
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
        SystemConfig sc = new SystemConfig(this.id);

        ae = new AlicaEngine(new IDManager(), roleSetName, sc, masterPlanName,false);
        bc = new BehaviourCreator();
        cc = new ConditionCreator();
        uc = new UtilityFunctionCreator();
        crc = new ConstraintCreator();

        ae.setAlicaClock(new AlicaClock());
        ae.addSolver(SolverType.GRADIENTSOLVER.ordinal(), SolverType.GRADIENTSOLVER); //new CGSolver(ae));
        ae.setCommunicator(new AlicaZMQCommunication(ae));
        ae.init(bc, cc, uc, crc);
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
