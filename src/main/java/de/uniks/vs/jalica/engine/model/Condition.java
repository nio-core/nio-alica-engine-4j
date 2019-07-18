package de.uniks.vs.jalica.engine.model;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.BasicCondition;
import de.uniks.vs.jalica.engine.BasicConstraint;
import de.uniks.vs.jalica.engine.RunningPlan;
import de.uniks.vs.jalica.engine.constrainmodule.ProblemDescriptor;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by alex on 20.07.17.
 * Updated 23.6.19
 */
public class Condition extends AlicaElement {

    protected BasicCondition basicCondition;
    protected BasicConstraint basicConstraint;
    protected ArrayList<Parameter> parameters;
    protected ArrayList<Variable> variables;
    protected ArrayList<Quantifier> quantifiers;

    protected AbstractPlan abstractPlan;

    protected String conditionString;
    protected String plugInName;

    public Condition() {
        this.abstractPlan = null;
        this.basicCondition = null;
        this.basicConstraint = null;
        this.parameters = new ArrayList<>();
        this.variables = new ArrayList<>();
        this.quantifiers = new ArrayList<>();
    }

    void getConstraint(ProblemDescriptor pd, RunningPlan rp) {
        // TODO: fix const cast below
        this.basicConstraint.getConstraint(pd, rp);
    }

    public void setConditionString(String conditionString)
    {
        this.conditionString = conditionString;
    }

    public boolean evaluate(RunningPlan rp) {

        if (this.basicCondition == null) {
            System.err.println("Condition: Missing implementation of condition: ID " + getID());
            return false;
        } else {
            boolean ret = false;
            try {
                // TODO: fix const cast below
                ret = this.basicCondition.evaluate(rp);
            } catch (Exception e) {
                System.err.println("C: Exception during evaluation catched: " + "\n" + e.getMessage());
            }
            if (CommonUtils.C_DEBUG_debug) System.out.println("C: evaluate " + ret);
            return ret;
        }
    }

    void setQuantifiers( ArrayList<Quantifier> quantifiers)
    {
        this.quantifiers = quantifiers;
    }

    void setVariables( ArrayList<Variable> variables)
    {
        this.variables = variables;
    }

    public void setAbstractPlan(AbstractPlan abstractPlan)
    {
        this.abstractPlan = abstractPlan;
    }

    public void setPlugInName(String plugInName)
    {
        this.plugInName = plugInName;
    }

    public void setBasicCondition(BasicCondition basicCondition)
    {
        this.basicCondition = basicCondition;
    }

    void setParameters( ArrayList<Parameter> parameters)
    {
        this.parameters = parameters;
    }

    public void setBasicConstraint(BasicConstraint basicConstraint)
    {
        this.basicConstraint = basicConstraint;
    }

    public AbstractPlan getAbstractPlan() {
        return this.abstractPlan;
    }

    String getConditionString() {
        return this.conditionString;
    }

    String getPlugInName() {
        return this.plugInName;
    }

    public ArrayList<Variable> getVariables() {
        return this.variables;
    }

    public ArrayList<Parameter> getParameters() {
        return this.parameters;
    }

    public ArrayList<Quantifier> getQuantifiers() {
        return this.quantifiers;
    }

    BasicCondition getBasicCondition() {
        return this.basicCondition;
    }

}
