package de.uniks.vs.jalica.engine;

/**
 * Created by alex on 18.07.17.
 * Updated 21.6.19
 */

public class PlanStatus {

    public enum Status {
        Success,
        Failed,
        Running
    }

    public static String getPlanStatusName(Status ps) {
        switch (ps) {
            case Running:
                return "Running";
            case Success:
                return "Success";
            case Failed:
                return "Failed";
        }
        return "Undefined";
    }

}
