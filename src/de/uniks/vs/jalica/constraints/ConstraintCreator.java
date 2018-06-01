package de.uniks.vs.jalica.constraints;

import de.uniks.vs.jalica.unknown.BasicConstraint;
import de.uniks.vs.jalica.unknown.CommonUtils;

/**
 * Created by alex on 13.07.17.
 */
public class ConstraintCreator {

    public ConstraintCreator() { }

    public BasicConstraint createConstraint(long constraintConfId) {

        switch ((int) constraintConfId) {

            default:
                System.out.println("ConstraintCreator: Unknown constraint requested: " + constraintConfId );
                CommonUtils.aboutError("");
                break;
        }
        return null;
    }
}
