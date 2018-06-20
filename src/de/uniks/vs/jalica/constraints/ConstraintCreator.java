package de.uniks.vs.jalica.constraints;

import de.uniks.vs.jalica.behaviours.helloworld.constraints.Constraint1470042926317;
import de.uniks.vs.jalica.behaviours.helloworld.constraints.Constraint1528124971225;
import de.uniks.vs.jalica.unknown.BasicConstraint;
import de.uniks.vs.jalica.unknown.CommonUtils;

/**
 * Created by alex on 13.07.17.
 */
public class ConstraintCreator {

    public ConstraintCreator() { }

    public BasicConstraint createConstraint(long constraintConfId) {

        if (constraintConfId == 1528124991817l) {
            return new Constraint1470042926317();
        }
        else if (constraintConfId == 1528124971225l) {
            return new Constraint1528124971225();
        }
        else {
            System.out.println("ConstraintCreator: Unknown constraint requested: " + constraintConfId );
            CommonUtils.aboutError("");
            return null;
        }
    }
}
