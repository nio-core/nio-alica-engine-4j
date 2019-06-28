package de.uniks.vs.jalica.engine.model;

import de.uniks.vs.jalica.engine.common.SolverVariable;

/**
 * Created by alex on 13.07.17.
 * Updated 23.6.19
 */
public class Variable extends AlicaElement {

    private String type;

    public Variable(long id, String name, String type) {
        super(id, name);
        this.type = type;
    }

    public String getType() {
        return type;
    }

    void setType(String type) {
        this.type = type;
    }


    @Override
    public String toString() {
        return   this.getName() + "(" + this.getID() + ")";
    }
}
