package de.uniks.vs.jalica.engine.model;

import de.uniks.vs.jalica.engine.BasicCondition;
import de.uniks.vs.jalica.engine.BasicConstraint;
import de.uniks.vs.jalica.engine.RunningPlan;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by alex on 20.07.17.
 */
public class Condition extends AlicaElement {

    private BasicCondition basicCondition;
    private AlicaElement abstractPlan;
    private String plugInName;
    private BasicConstraint basicConstraint;
    private ArrayList<Parameter> parameters = new ArrayList<>();
    protected ArrayList<Quantifier> quantifiers = new ArrayList<>();
    protected Vector<Variable> variables = new Vector<>();

    protected String conditionString;
    private Transition transition;

    public Condition() {}

    public boolean evaluate(RunningPlan rp) {
        if (basicCondition == null)
        {
            System.err.println( "Condition: Missing implementation of condition: ID " + this.getID() );
            return false;
        }
        else
        {
            boolean ret = false;
            try {
                ret = basicCondition.evaluate(rp);
            } catch (Exception e) {
                System.err.println( "Condition: Exception during evaluation catched: \n" + e.getMessage() );
        }
            return ret;
        }
    }

    public Vector<Variable> getVariables() {
        return variables;
    }

    public ArrayList<Quantifier> getQuantifiers() {
        return quantifiers;
    }

    public AlicaElement getAbstractPlan() {
        return abstractPlan;
    }

    public void setConditionString(String conditionString) {
        this.conditionString = conditionString;
    }

    public void setPlugInName(String plugInName) {
        this.plugInName = plugInName;
    }

    public ArrayList<Parameter> getParameters() {
        return parameters;
    }

    public void setAbstractPlan(AbstractPlan abstractPlan) {
        this.abstractPlan = abstractPlan;
    }

    public void setBasicCondition(BasicCondition basicCondition) {
        this.basicCondition = basicCondition;
    }

    public BasicCondition getBasicCondition() {
        return basicCondition;
    }

    public void setBasicConstraint(BasicConstraint basicConstraint) {
        this.basicConstraint = basicConstraint;
    }

    public void setTransition(Transition transition) {

        if (this.transition == transition)
            return;
        this.transition = transition;
    }
}
