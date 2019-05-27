package de.uniks.vs.jalica.engine.common;

import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.model.*;

import java.util.ArrayList;

/**
 * Created by alex on 13.07.17.
 */
public class PlanningProblem extends AbstractPlan {

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
    private ArrayList<AbstractPlan> plans = new ArrayList<>();

    public PlanningProblem(AlicaEngine ae) { super(ae); }

    public ArrayList<AbstractPlan> getPlans() {
        return plans;
    }

    public void setWaitPlan(Plan waitPlan) {
        this.waitPlan = waitPlan;
    }

    public void setAlternativePlan(Plan alternativePlan) {
        this.alternativePlan = alternativePlan;
    }

    public void setUpdateRate(int updateRate) {
        this.updateRate = updateRate;
    }

    public int getUpdateRate() {
        return updateRate;
    }

    public void setDistributeProblem(boolean distributeProblem) {
        this.distributeProblem = distributeProblem;
    }

    public void setPlanningType(PlanningType planningType) {
        this.planningType = planningType;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public void setPostCondition(PostCondition postCondition) {
        this.postCondition = postCondition;
    }
}
