package de.uniks.vs.jalica.unknown;

import java.util.ArrayList;

/**
 * Created by alex on 13.07.17.
 */
public class State extends AlicaElement {

    protected boolean successState;
    protected boolean failureState;
    protected boolean terminal;
    protected boolean finished;
    protected ArrayList<Transition> outTransitions = new ArrayList<>();
    protected ArrayList<Transition> inTransitions = new ArrayList<>();
    protected ArrayList<AbstractPlan> plans = new ArrayList<>();
    protected ArrayList<Parametrisation> parametrisation = new ArrayList<>();
    protected Plan inPlan;
    protected EntryPoint entryPoint;

    public State() {
        this.terminal = false;
        this.successState = false;
        this.failureState = false;
        this.inPlan = null;
        this.entryPoint = null;
    }

    public State(long id) {
        this();
        this.id = id;
    }

    public boolean isSuccessState() {
        return successState;
    }

    public void setSuccessState(boolean successState) {
        this.successState = successState;
    }

    public boolean isFailureState() {
        return failureState;
    }

    public void setFailureState(boolean failureState) {
        this.failureState = failureState;
    }

    public ArrayList<Transition> getOutTransitions() {
        return outTransitions;
    }

    public void addOutTransition(Transition transition) {

        if (outTransitions.contains(transition))
            return;
        outTransitions.add(transition);
        transition.setInState(this);
    }

    public void removeOutTransition(Transition transition) {

        if (!this.outTransitions.contains(transition))
            return;
        this.outTransitions.remove(transition);
        transition.deleteInState(this);
    }

    public void setOutTransitions(ArrayList<Transition> outTransitions) {
        this.outTransitions = outTransitions;
    }

    public ArrayList<Transition> getInTransitions() {
        return inTransitions;
    }

    public void addInTransition(Transition transition) {

        if (inTransitions.contains(transition))
            return;
        inTransitions.add(transition);
        transition.setInState(this);
    }

    public void removeInTransition(Transition transition) {

        if (!this.inTransitions.contains(transition))
            return;
        this.inTransitions.remove(transition);
        transition.deleteInState(this);
    }

    public void setInTransitions(ArrayList<Transition> inTransitions) {
        this.inTransitions = inTransitions;
    }

    public ArrayList<AbstractPlan> getPlans() {
        return plans;
    }

    public void setPlans(ArrayList<AbstractPlan> plans) {
        this.plans = plans;
    }

    public ArrayList<Parametrisation> getParametrisation() {
        return parametrisation;
    }

    public void setParametrisation(ArrayList<Parametrisation> parametrisation) {
        this.parametrisation = parametrisation;
    }

    public Plan getInPlan() {
        return inPlan;
    }

    public void setInPlan(Plan inPlan) {
        this.inPlan = inPlan;
    }

    public EntryPoint getEntryPoint() {
        return entryPoint;
    }

    public void setEntryPoint(EntryPoint entryPoint) {
        this.entryPoint = entryPoint;
    }

    public void setTerminal(boolean terminal) {
        this.terminal = terminal;
    }
}
