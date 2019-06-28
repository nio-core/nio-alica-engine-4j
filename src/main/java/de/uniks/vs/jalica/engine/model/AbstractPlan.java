package de.uniks.vs.jalica.engine.model;

import de.uniks.vs.jalica.engine.UtilityFunction;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.AlicaTime;

import java.util.ArrayList;

/**
 * Created by alex on 17.07.17.
 * updated 21.6.19
 */
public class AbstractPlan extends AlicaElement {

    private  ArrayList<Variable> variables;
    private String fileName;
    // TODO: move this to the authority module
    private AlicaTime authorityTimeInterval;

//    private PreCondition preCondition;
//    private RuntimeCondition runtimeCondition;
//    private UtilityFunction utilityFunction;
//    private double utilityThreshold;
//    private String currentFile;
//    private boolean masterPlan;
//    private AlicaTime authorityTimeInterval;
//    private ArrayList<Variable> variables;

    public AbstractPlan(AlicaEngine ae) {
        super();
        this.variables = new ArrayList<>();
        long minAuthorityInterval = Long.valueOf((String) ae.getSystemConfig().get("Alica").get("Alica.CycleDetection.MinimalAuthorityTimeInterval"));
        this.authorityTimeInterval = new AlicaTime().inMilliseconds(minAuthorityInterval);
    }

    public AbstractPlan(AlicaEngine ae, long id) {
        super(id);
        this.variables = new ArrayList<>();
        long minAuthorityInterval = Long.valueOf((String) ae.getSystemConfig().get("Alica").get("Alica.CycleDetection.MinimalAuthorityTimeInterval"));
        this.authorityTimeInterval = new AlicaTime().inMilliseconds(minAuthorityInterval);
    }

    public String toString() {
        String indent = "";
        String ss = "";
        ss += indent + super.toString();
        ss += indent + "\tFilename: " + this.fileName + "\n";
        return ss;
    }

    public Variable getVariable(String name) {

        for (Variable variable : this.variables) {

            if (variable.getName() == name) {
                return variable;
            }
        }
        return null;
    }

    boolean containsVar(Variable v) {
        return this.variables.contains(v);
    }

    boolean containsVar(String name) {
        for (Variable v : this.variables) {
            if (v.getName() == name) {
                return true;
            }
        }
        return false;
    }

    public void setAuthorityTimeInterval(AlicaTime authorithyTimeInterval) {
        this.authorityTimeInterval = authorithyTimeInterval;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ArrayList<Variable> getVariables() {
        return variables;
    }


    public AlicaTime getAuthorityTimeInterval() { return authorityTimeInterval;}

    public String getFileName()  { return this.fileName; }
}
