package de.uniks.vs.jalica.engine.model;


import java.util.ArrayList;

/**
 * Created by alex on 13.07.17.
 * updated 23.6.19
 */
public class State extends AlicaElement {

    enum StateType {
        NORMAL,
        SUCCESS,
        FAILURE
    };

    protected ArrayList<AbstractPlan> plans;
    protected ArrayList<Transition> inTransitions;
    protected ArrayList<Transition> outTransitions;
    protected ArrayList<VariableBinding> variableBindingGrp;
    protected Plan inPlan;
    protected EntryPoint entryPoint;
    protected StateType type;

    public State() {
        super(0);
        this.inPlan = null;
        this.entryPoint = null;
        this.type = StateType.NORMAL;
        this.plans = new ArrayList<>();
        this.inTransitions = new ArrayList<>();
        this.outTransitions = new ArrayList<>();
        this.variableBindingGrp = new ArrayList<>();
    }

    public State(StateType t) {
        super(0);
        this.inPlan = null;
        this.entryPoint = null;
        this.type = t;
        this.plans = new ArrayList<>();
        this.inTransitions = new ArrayList<>();
        this.outTransitions = new ArrayList<>();
        this.variableBindingGrp = new ArrayList<>();
    }

    public void setInPlan(Plan inPlan) {
        this.inPlan = inPlan;
    }

    void setInTransitions(ArrayList<Transition> inTransitions) {
        this.inTransitions = inTransitions;
    }

    void setOutTransitions(ArrayList<Transition> outTransition) {
        this.outTransitions = outTransition;
    }

    void setVariableBindings(ArrayList<VariableBinding> variableBindingGrp) {
        this.variableBindingGrp = variableBindingGrp;
    }

    void setPlans(ArrayList<AbstractPlan> plans) {
        this.plans = plans;
    }

     public Plan getInPlan()  { return this.inPlan; }
     public EntryPoint getEntryPoint()  { return this. entryPoint; }
     public ArrayList<AbstractPlan> getPlans()  { return this. plans; }
     public ArrayList<Transition> getInTransitions()  { return this. inTransitions; }
     public ArrayList<Transition> getOutTransitions()  { return this. outTransitions; }
     public ArrayList<VariableBinding> getParametrisation()  { return this. variableBindingGrp; }

    boolean isTerminal()  { return this. type != StateType.NORMAL; }
    public boolean isSuccessState()  { return this. type == StateType.SUCCESS; }
    public boolean isFailureState()  { return this. type == StateType.FAILURE; }

    public void setEntryPoint(EntryPoint entryPoint) {
        this.entryPoint = entryPoint;
    }

    @Override
    public String toString() {
        String indent = "";
        String  ss = "";
        ss += indent + "#State: " + getName() + " " + getID() + "\n";
        ss +=indent + "\tParent Plan: " + ((AlicaElement)this.inPlan).getName() + " " + ((AlicaElement)this.inPlan).getID() + "\n";
        ss +=indent + "\tInTransitions: " + "\n";
        for ( Transition trans : this.inTransitions) {
            ss +=((AlicaElement)trans).toString();
        }
        ss +=indent + "\tOutTransitions: " + "\n";
        for ( Transition trans : this.outTransitions) {
            ss +=((AlicaElement)trans).toString();
        }
        ss +=indent + "\tAbstract Plans: " + "\n";
        for ( AbstractPlan plans : this.plans) {
            ss +=((AlicaElement)plans).toString();
        }
        ss +=indent + "\tVariable Bindings: " + "\n";
        for ( VariableBinding binding : this.variableBindingGrp) {
            ss +=((AlicaElement)binding).toString();
        }
        ss +=indent + "#EndState" + "\n";
        return ss;
    }
}
