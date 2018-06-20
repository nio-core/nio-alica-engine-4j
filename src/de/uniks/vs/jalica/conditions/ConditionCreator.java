package de.uniks.vs.jalica.conditions;

import de.uniks.vs.jalica.behaviours.helloworld.testcommunication1528124991817.TransitionCondition1528125076265;
import de.uniks.vs.jalica.behaviours.helloworld.testcommunicationmaster1528124971225.TransitionCondition1528125190720;
import de.uniks.vs.jalica.unknown.BasicCondition;
import de.uniks.vs.jalica.unknown.CommonUtils;

/**
 * Created by alex on 13.07.17.
 */
public class ConditionCreator {

    public ConditionCreator() { }

    public BasicCondition createConditions(long conditionConfId) {

        if (conditionConfId == 1528125190720l) {
            return new TransitionCondition1528125190720();
        }
        else if (conditionConfId == 1528125076265l) {
            return new TransitionCondition1528125076265();
        }
        else {
            System.out.println("ConditionCreator: Unknown condition requested: " + conditionConfId);
            CommonUtils.aboutError("");
            return null;
        }
    }
}
