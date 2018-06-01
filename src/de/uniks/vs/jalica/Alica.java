package de.uniks.vs.jalica;

/**
 * Created by alex on 13.07.17.
 */
public class Alica extends Thread {

    private String id;

    public Alica() {
    }

    public Alica(String id) {
        this.id = id;
    }

    public static void main(String... param) {
        Alica alica = new Alica(param[0]);
        alica.start();
    }

    protected void init() {
        String roleSetName = "Roleset";
        String masterPlanName =  "DummyPlan";
        String roleSetDir = "roles/";
        boolean sim = true;

        System.out.println("\tMasterplan is:       \"" + masterPlanName + "\"");
        System.out.println("\tRolset Directory is: \"" + roleSetDir + "\"");
        System.out.println("\tRolset is:           \"" + (roleSetName.isEmpty() ? "Default" : roleSetName) + "\"");
        System.out.println("\nConstructing Base ...");
        System.out.println("\nStarting Base ...");

        Base base = new Base(this.id, roleSetName, masterPlanName, roleSetDir, sim);
        base.start();
    }

    @Override
    public void run() {
        init();
        //        // STRG c abfangen
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
////            std::chrono::milliseconds dura(500);
////            std::this_thread::sleep_for(dura);
        }
    }
}

