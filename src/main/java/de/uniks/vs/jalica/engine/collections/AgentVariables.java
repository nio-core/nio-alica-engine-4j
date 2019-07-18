package de.uniks.vs.jalica.engine.collections;

import de.uniks.vs.jalica.engine.idmanagement.ID;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class AgentVariables<T> {

    private ID id;
    private ArrayList<T> vars;

    public AgentVariables(ID id) {
        this.id = id;
        this.vars = new ArrayList<>();
    }

    AgentVariables(AgentVariables o){
        this.id = (o.id);
        this.vars = new ArrayList<>(o.vars); // std::move
    }

    public ID getId()  { return this.id; }

    public ArrayList<T> getVars() { return this.vars; }

}

//    using AgentVariables = AgentVariables<DomainVariable>;
//    using AgentSolverVariables = AgentVariables<SolverVariable>;