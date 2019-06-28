package de.uniks.vs.jalica.engine.model;

import de.uniks.vs.jalica.engine.AlicaEngine;

import java.util.ArrayList;

/**
 * Created by alex on 13.07.17.
 * updated 21.6.19
 */
public class PlanType extends AbstractPlan {

    private ArrayList<Plan> plans = new ArrayList<>();
    private ArrayList<VariableBinding> variableBindings = new ArrayList<>();

    public PlanType(AlicaEngine ae) {
        super(ae);
    }

    public ArrayList<VariableBinding> getVariableBindings()  { return this.variableBindings; }
    public ArrayList<Plan> getPlans()  { return this.plans; }
    public Plan getPlanById(long id) {

        for ( Plan p : this.plans) {

            if (p.getID() == id) {
                return p;
            }
        }
        return null;
    }


    private void setVariableBindings( ArrayList<VariableBinding>  variableBindings) {
        this.variableBindings = variableBindings;
    }

    private void setPlans(ArrayList<Plan> plans) {
        this.plans = plans;
    }

    public String toString() {
        String indent = "";
        String ss ="";
        ss += indent + "#PlanType: " + getName() + " " + getID() + "\n";
        ss += indent + "\t Plans: " + this.plans.size() + "\n";
        if (this.plans.size() != 0) {
            for ( Plan p : this.plans) {
                ss += indent + "\t" + p.getID() + " " + p.getName() + "\n";
            }
        }
        ss += indent + "#EndPlanType" + "\n";
        return ss;
    }
}
