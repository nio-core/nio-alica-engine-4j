package de.uniks.vs.jalica.engine.model;

import de.uniks.vs.jalica.engine.UtilityFunction;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.AlicaTime;

import java.util.ArrayList;

/**
 * Created by alex on 17.07.17.
 */
public class AbstractPlan extends AlicaElement {
    private PreCondition preCondition;
    private RuntimeCondition runtimeCondition;
    private UtilityFunction utilityFunction;
    private double utilityThreshold;
    private String currentFile;
    private boolean masterPlan;
    private AlicaTime authorityTimeInterval;
    private ArrayList<Variable> variables;

    public AbstractPlan(AlicaEngine ae) {
        super();
        this.masterPlan = false;
        this.variables = new ArrayList<>();
//        SystemConfig systemConfig = SystemConfig.getInstance();
        long minAuthorityInterval = Long.valueOf((String) ae.getSystemConfig().get("Alica").get("Alica.CycleDetection.MinimalAuthorityTimeInterval"));
        this.authorityTimeInterval = new AlicaTime().inMilliseconds(minAuthorityInterval);
//    Long.valueOf((String) ae.getSystemConfig().get("Alica").get("Alica.CycleDetection.MinimalAuthorityTimeInterval")) * 1000000);
    }

    public PreCondition getPreCondition() {
        return preCondition;
    }

    public RuntimeCondition getRuntimeCondition() {
        return runtimeCondition;
    }

    public UtilityFunction getUtilityFunction() {
        return utilityFunction;
    }

    public void setUtilityFunction(UtilityFunction utilityFunction) {
        this.utilityFunction = utilityFunction;
    }

    public double getUtilityThreshold() {
        return utilityThreshold;
    }

    public void setFileName(String currentFile) {
        this.currentFile = currentFile;
    }

    public void setMasterPlan(boolean masterPlan) {
        this.masterPlan = masterPlan;
    }

    public void setUtilityThreshold(double utilityThreshold) {
        this.utilityThreshold = utilityThreshold;
    }

    public void setRuntimeCondition(RuntimeCondition runtimeCondition) {
        this.runtimeCondition = runtimeCondition;
    }

    public void setPreCondition(PreCondition preCondition) {
        this.preCondition = preCondition;
    }

    public ArrayList<Variable> getVariables() {
        return variables;
    }

    public void setAuthorityTimeInterval(AlicaTime authorityTimeInterval) { this.authorityTimeInterval = authorityTimeInterval;}

    public AlicaTime getAuthorityTimeInterval() { return authorityTimeInterval;}
}
