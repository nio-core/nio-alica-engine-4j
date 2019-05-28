package de.uniks.vs.jalica.behaviours;

import de.uniks.vs.jalica.behaviours.conditions.TransitionCondition1402488519140;
import de.uniks.vs.jalica.behaviours.conditions.TransitionCondition1414403842622;
import de.uniks.vs.jalica.behaviours.helloworld.testcommunication1528124991817.TransitionCondition1528125076265;
import de.uniks.vs.jalica.behaviours.helloworld.testcommunicationmaster1528124971225.TransitionCondition1528125190720;
import de.uniks.vs.jalica.engine.BasicCondition;
import de.uniks.vs.jalica.engine.IConditionCreator;
import de.uniks.vs.jalica.common.utils.CommonUtils;

/**
 * Created by alex on 13.07.17.
 */
public class ConditionCreator implements IConditionCreator {

    public ConditionCreator() { }

    @Override
    public BasicCondition createConditions(long conditionConfId) {

        if (conditionConfId == 1528125190720l) {
            return new TransitionCondition1528125190720();
        }
        else if (conditionConfId == 1528125076265l) {
            return new TransitionCondition1528125076265();
        }
        else if (conditionConfId == 1402488993122l) {
            return new TransitionCondition1402488519140();
        }
        else if (conditionConfId == 1414403842622l) {
            return new TransitionCondition1414403842622();
        }
        else if (conditionConfId == 1402488519140l) {
            return new DummyTransitionCondition();
        }
        else if (conditionConfId == 1402488520968l) {
            return new DummyTransitionCondition();
        }
        else if (conditionConfId == 1402488558741l) {
            return new DummyTransitionCondition();
        }
        else if (conditionConfId == 1409218319990l) {
            return new DummyTransitionCondition();
        }
        else if (conditionConfId == 1402488991641l) {
            return new DummyTransitionCondition();
        }
        else if (conditionConfId == 1402489065962l) {
            return new DummyTransitionCondition();
        }
        else if (conditionConfId == 1402489073613l) {
            return new DummyTransitionCondition();
        }
        else if (conditionConfId == 1402489131988l) {
            return new DummyTransitionCondition();
        }
        else if (conditionConfId == 1403773741874l) {
            return new DummyTransitionCondition();
        }
        else if (conditionConfId == 1402489174338l) {
            return new DummyTransitionCondition();
        }
        else if (conditionConfId == 1402489206278l) {
            return new DummyTransitionCondition();
        }
        else if (conditionConfId == 1402489218027l) {
            return new DummyTransitionCondition();
        }
        else if (conditionConfId == 1402489260911l) {
            return new DummyTransitionCondition();
        }
        else if (conditionConfId == 1402489258509l) {
            return new DummyTransitionCondition();
        }
        else if (conditionConfId == 1402489278408l) {
            return new DummyTransitionCondition();
        }
        else if (conditionConfId == 1402500844446l) {
            return new DummyTransitionCondition();
        }
        else {
//            System.out.println("ConditionCreator: Unknown condition requested: " + conditionConfId);
            CommonUtils.aboutError("ConditionCreator: Unknown condition requested: " + conditionConfId);
            return new DummyTransitionCondition();
        }
    }
}
