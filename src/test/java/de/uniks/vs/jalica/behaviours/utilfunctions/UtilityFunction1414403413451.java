package de.uniks.vs.jalica.behaviours.utilfunctions;

import de.uniks.vs.jalica.behaviours.DummyTestSummand;
import de.uniks.vs.jalica.engine.USummand;
import de.uniks.vs.jalica.engine.UtilityFunction;
import de.uniks.vs.jalica.engine.BasicUtilityFunction;
import de.uniks.vs.jalica.engine.model.Plan;

import java.util.ArrayList;
import java.util.Vector;

public class UtilityFunction1414403413451 extends BasicUtilityFunction {

    //Plan:AuthorityTest

    /* generated comment

     Task: DefaultTask  -> EntryPoint-ID: 1414403429951

     Task: AttackTask  -> EntryPoint-ID: 1414403522424

     */
    public UtilityFunction getUtilityFunction(Plan plan) {
        /*PROTECTED REGION ID(1414403413451) ENABLED START*/
        Vector<Long> relevantEntryPoints = new Vector<>();
        relevantEntryPoints.add(1414403522424l);
        relevantEntryPoints.add(1414403429951l);

        DummyTestSummand us = new DummyTestSummand(1.0, "Something", 1, relevantEntryPoints);
        ArrayList<USummand> utilSummands = new ArrayList<>();
        utilSummands.add(us);
        UtilityFunction function = new UtilityFunction(0.1, 0.1, plan);

        return function;

        /*PROTECTED REGION END*/
    }

//State: UpperState in Plan: AuthorityTest

//State: LowerState in Plan: AuthorityTest
}
