package de.uniks.vs.jalica.reasoner;

import de.uniks.vs.jalica.unknown.SolverVariable;

import java.util.Vector;

/**
 * Created by alex on 10.11.17.
 */
public class ProblemDescriptor {

    int dim;
    final double min = -10E29;
    final double max = 10E29;

    double utilitySignificanceThreshold = 1E-22; /*<< minimum delta for adapting a better utility */
    boolean setsUtilitySignificanceThreshold;

    SolverTerm constraint;
    SolverTerm utility;
    double utilitySufficiencyThreshold;
    Vector<SolverVariable> staticVars;
    Vector<Vector<Vector<SolverVariable>>> domainVars;
    Vector<Vector<Integer>> agentsInScope;
    Vector<SolverVariable> allVars;

    Vector<Vector<Vector<Vector<Double>>>> domainRanges;
    Vector<Vector<Double>> staticRanges;

}
