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
    private ArrayList<Parametrisation> parametrisation;
    private Plan inPlan;

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

    public ArrayList<Parametrisation> getParametrisation() {
        return parametrisation;
    }

    public void setInPlan(Plan inPlan) {
        this.inPlan = inPlan;
    }
}
