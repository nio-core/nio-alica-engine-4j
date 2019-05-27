package de.uniks.vs.jalica.engine.constrainmodule;

import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.model.Variable;

import java.util.Vector;

/**
 * Created by alex on 10.11.17.
 */
public class Query {

    UniqueVarStore uniqueVarStore;
    Vector<Variable> queriedStaticVariables;
    Vector<Variable> queriedDomainVariables;
    Vector<ProblemPart> problemParts;

    Vector<Variable> relevantStaticVariables;
    Vector<Variable> relevantDomainVariables;

    AlicaEngine ae;
}
