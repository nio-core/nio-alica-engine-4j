package de.uniks.vs.jalica.unknown;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by alex on 20.07.17.
 */
public class Condition extends AlicaElement{

    private BasicCondition basicCondition;
    private Vector<Variable> variables;
    private ArrayList<Quantifier> quantifiers;
    private AlicaElement abstractPlan;

    public boolean evaluate(RunningPlan rp) {
        if (basicCondition == null)
        {
            System.err.println( "Condition: Missing implementation of condition: ID " + this.getId() );
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
}
