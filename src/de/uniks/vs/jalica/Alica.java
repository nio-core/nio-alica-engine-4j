package de.uniks.vs.jalica;

/**
 * Created by alex on 13.07.17.
 */
public class Alica {


    public Alica() {}

    public static void main(String... param) {
        Alica alica = new Alica();
        alica.start();
    }

    private void start() {
        String roleSetName = "Roleset";
//        String masterPlanName = "WM16";
        String masterPlanName =  "DummyPlan";
        String roleSetDir = "roles/";
        boolean sim = false;

        System.out.println("\tMasterplan is:       \"" + masterPlanName + "\"" );
        System.out.println( "\tRolset Directory is: \"" + roleSetDir + "\"" );
        System.out.println( "\tRolset is:           \"" + (roleSetName.isEmpty() ? "Default" : roleSetName) + "\"" );

        System.out.println( "\nConstructing Base ...");

        System.out.println( "\nStarting Base ...");

        Base base = new Base(roleSetName,  masterPlanName,  roleSetDir,  sim);
        base.start();

//        // STRG c abfangen
//        while (true)
//        {
////            std::chrono::milliseconds dura(500);
////            std::this_thread::sleep_for(dura);
//        }
    }
}

