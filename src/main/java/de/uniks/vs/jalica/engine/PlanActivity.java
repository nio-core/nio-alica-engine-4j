package de.uniks.vs.jalica.engine;

public class PlanActivity {

    public enum Activity {
        InActive,
        Active,
        Retired
    }

    public static String getPlanActivityName(Activity pa) {
        switch (pa) {
            case InActive:
                return "InActive";
            case Active:
                return "Active";
            case Retired:
                return "Retired";
        }
        return "Undefined";
    }
}

