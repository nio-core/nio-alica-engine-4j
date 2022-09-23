package de.uniks.vs.jalica.behaviours;

import de.uniks.vs.jalica.behaviours.constraints.Constraint1403773741874;
import de.uniks.vs.jalica.behaviours.helloworld.constraints.Constraint1470042926317;
import de.uniks.vs.jalica.behaviours.helloworld.constraints.Constraint1528124971225;
import de.uniks.vs.jalica.engine.BasicConstraint;
import de.uniks.vs.jalica.engine.IConstraintCreator;
import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.expressions.DummyConstraint;

/**
 * Created by alex on 13.07.17.
 */
public class ConstraintCreator implements IConstraintCreator {

    public ConstraintCreator() { }

    @Override
    public BasicConstraint createConstraint(long constraintConfId, Object context) {

        if (constraintConfId == 1528124991817l) {
            return new Constraint1470042926317();
        }
        else if (constraintConfId == 1528124971225l) {
            return new Constraint1528124971225();
        }
        else if (constraintConfId == 1403773741874l) {
            return new Constraint1403773741874();
        }
        else {
//            System.out.println("ConstraintCreator: Unknown constraint requested: " + constraintConfId );
            CommonUtils.aboutError("ConstraintCreator: Unknown constraint requested: " + constraintConfId);
            return new DummyConstraint();
        }
    }
}
