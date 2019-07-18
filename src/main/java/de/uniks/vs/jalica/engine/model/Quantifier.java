package de.uniks.vs.jalica.engine.model;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.RunningPlan;
import de.uniks.vs.jalica.engine.collections.AgentVariables;
import de.uniks.vs.jalica.engine.idmanagement.ID;

import java.util.ArrayList;

/**
 * Created by alex on 13.07.17.
 */
public abstract class Quantifier extends AlicaElement {

    protected enum Scope {
        PLANSCOPE,
        ENTRYPOINTSCOPE,
        STATESCOPE
    }

    private ArrayList<String> domainIdentifiers = new ArrayList<>();
    ArrayList<Variable> templateVars;
    AlicaElement scope;
    Scope scopeType;

    public Quantifier() {
        this.scope = null;
        this.scopeType = Scope.PLANSCOPE;
    }

    abstract boolean isAgentInScope(ID id, RunningPlan runningPlan) ;
    abstract boolean addDomainVariables(RunningPlan p, ArrayList<AgentVariables> ioAgentVarsInScope);

    public void setScope( AlicaElement element) {

        this.scope = element;
        if (element instanceof  EntryPoint) {
            this.scopeType = Scope.ENTRYPOINTSCOPE;
        } else if (element instanceof Plan) {
            this.scopeType = Scope.PLANSCOPE;
        } else if (element instanceof  State) {
            this.scopeType = Scope.STATESCOPE;
        } else {
            CommonUtils.aboutError("Scope of Quantifier is not an entrypoint, plan, or state: " + element);
        }
    }

    public ArrayList<String> getDomainIdentifiers() { return this.domainIdentifiers; }
    public boolean isScopeEntryPoint()  { return this.scopeType == Scope.ENTRYPOINTSCOPE; }
    public boolean isScopePlan()  { return this.scopeType == Scope.PLANSCOPE; }
    public boolean isScopeState()  { return this.scopeType == Scope.STATESCOPE; }
    public  State getScopedState()  { return this.scopeType == Scope.STATESCOPE ? (State)this.scope : null; }
    public  EntryPoint getScopedEntryPoint()  { return this.scopeType == Scope.ENTRYPOINTSCOPE ? (EntryPoint)this.scope : null; }
    public  Plan getScopedPlan()  { return this.scopeType == Scope.PLANSCOPE ? (Plan)this.scope : null; }
    public  AlicaElement getScope()  { return this.scope; }
    public  ArrayList<Variable> getTemplateVariables()  { return this.templateVars; }
    public boolean hasTemplateVariable( Variable variable)  { return this.templateVars.contains(variable); }
    protected Scope getScopeType() { return this.scopeType; }
    private void setDomainIdentifiers(ArrayList<String> domainIdentifiers) {
        this.domainIdentifiers = domainIdentifiers;
    }
}
