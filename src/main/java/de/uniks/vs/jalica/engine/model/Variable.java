package de.uniks.vs.jalica.engine.model;

import de.uniks.vs.jalica.engine.common.SolverVariable;

/**
 * Created by alex on 13.07.17.
 */
public class Variable extends AlicaElement {
    private final String type;
    private final SolverVariable solverVar;

    public Variable(long id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
// TODO:		this.solverVar = new autodiff::Variable();
        this.solverVar = null;
    }
}
