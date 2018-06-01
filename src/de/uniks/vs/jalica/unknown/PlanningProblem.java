package de.uniks.vs.jalica.unknown;

import com.sun.org.apache.bcel.internal.generic.ALOAD;
import de.uniks.vs.jalica.engine.AlicaEngine;

import java.util.ArrayList;

/**
 * Created by alex on 13.07.17.
 */
public class PlanningProblem extends AbstractPlan {

    private ArrayList<AbstractPlan> plans;
    private Plan alternativePlan;
    private Plan waitPlan;
    private int updateRate;
    private boolean distributeProblem;
    private PlanningType planningType;
    private String requirements;
    private String fileName;
    private PostCondition postCondition;
    private PreCondition preCondition;
    private RuntimeCondition runtimeCondition;

    public PlanningProblem(AlicaEngine ae) {
        super(ae);
    }

    public ArrayList<AbstractPlan> getPlans() {
        return plans;
    }

    public void setWaitPlan(Plan waitPlan) {
        this.waitPlan = waitPlan;
    }

    public void setAlternativePlan(Plan alternativePlan) {
        this.alternativePlan = alternativePlan;
    }
}
