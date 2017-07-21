package de.uniks.vs.jalica.unknown;

import java.security.KeyStore;
import java.util.ArrayList;

/**
 * Created by alex on 13.07.17.
 */
public class State extends AlicaElement{

    private boolean successState;
    private boolean failureState;
    private ArrayList<Transition> outTransitions;
    private ArrayList<AbstractPlan> plans;

    public boolean isSuccessState() {
        return successState;
    }

    public boolean isFailureState() {
        return failureState;
    }

    public ArrayList<Transition> getOutTransitions() {
        return outTransitions;
    }

    public ArrayList<AbstractPlan> getPlans() {
        return plans;
    }
}
