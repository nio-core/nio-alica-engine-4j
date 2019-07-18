package de.uniks.vs.jalica.engine.model;

import de.uniks.vs.jalica.engine.idmanagement.ID;

import java.util.Vector;

public class DomainVariable extends Variable {

    private Variable templateVar;
    private ID agentId;

    public DomainVariable(long id, String name, String type,  Variable templateVar, ID agent) {
        super(id, name, type);
        this.templateVar = templateVar;
        this.agentId = agent;
    }

    public Variable getTemplateVariable() { return this.templateVar; }
    public ID getAgentID()  { return this.agentId; }
}
