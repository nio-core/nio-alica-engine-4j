package de.uniks.vs.jalica.unknown;

import java.util.ArrayList;

/**
 * Created by alex on 13.07.17.
 */
public class State extends AlicaElement{

    private boolean successState;
    private boolean failureState;
    private ArrayList<Transition> outTransitions = new ArrayList<>();
    private ArrayList<Transition> inTransitions = new ArrayList<>();
    private ArrayList<AbstractPlan> plans = new ArrayList<>();
    private ArrayList<Parametrisation> parametrisation = new ArrayList<>();
    private Plan inPlan;
    private EntryPoint entryPoint;

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

    public ArrayList<Transition> getInTransitions() {
        return inTransitions;
    }

    public void setEntryPoint(EntryPoint entryPoint) {
        this.entryPoint = entryPoint;
    }

    public EntryPoint getEntryPoint() {
        return entryPoint;
    }

    public AbstractPlan getInPlan() {return inPlan;}
}
