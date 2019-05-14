package de.uniks.vs.jalica.behaviours;

import de.uniks.vs.jalica.behaviours.tests.UtilityFunction1402488437260;
import de.uniks.vs.jalica.behaviours.helloworld.testcommunication1528124991817.UtilityFunction1528124991817;
import de.uniks.vs.jalica.behaviours.helloworld.testcommunicationmaster1528124971225.UtilityFunction1528124971225;
import de.uniks.vs.jalica.behaviours.utilfunctions.UtilityFunction1414403396328;
import de.uniks.vs.jalica.behaviours.utilfunctions.UtilityFunction1414403413451;
import de.uniks.vs.jalica.unknown.BasicUtilityFunction;
import de.uniks.vs.jalica.unknown.CommonUtils;
import de.uniks.vs.jalica.unknown.UtilityFunction1482486227468;

/**
 * Created by alex on 13.07.17.
 */
public class UtilityFunctionCreator {

    public UtilityFunctionCreator() { }

    public BasicUtilityFunction createUtility(long utilityfunctionConfId) {

        if (utilityfunctionConfId == 1482486227468l) {
            return new UtilityFunction1482486227468();
        }
        else if (utilityfunctionConfId == 1528124971225l) {
            return new UtilityFunction1528124971225();
        }
        else if (utilityfunctionConfId == 1528124991817l) {
                return new UtilityFunction1528124991817();
        }
        else if (utilityfunctionConfId == 1402488437260l) {
                return new UtilityFunction1402488437260();
        }
        else if (utilityfunctionConfId == 1414403413451l) {
            return new UtilityFunction1414403413451();
        }
        else if (utilityfunctionConfId == 1414403396328l) {
            return new UtilityFunction1414403396328();
        }
        else if (utilityfunctionConfId == 1402488893641l) {
            return new DummyUtilityFunction();
        }
        else if (utilityfunctionConfId == 1402488870347l) {
            return new DummyUtilityFunction();
        }
        else if (utilityfunctionConfId == 1402488770050l) {
            return new DummyUtilityFunction();
        }
        else if (utilityfunctionConfId == 1402489318663l) {
            return new DummyUtilityFunction();
        }
        else if (utilityfunctionConfId == 1407152758497l) {
            return new DummyUtilityFunction();
        }
        else if (utilityfunctionConfId == 1407153611768l) {
            return new DummyUtilityFunction();
        }
        else if (utilityfunctionConfId == 1407153645238l) {
            return new DummyUtilityFunction();
        }
        else if (utilityfunctionConfId == 1402488634525l) {
            return new DummyUtilityFunction();
        }

        else {
//            System.out.println("UtilityFunctionCreator: Unknown utility requested: " + utilityfunctionConfId);
            CommonUtils.aboutError("UtilityFunctionCreator: Unknown utility requested: " + utilityfunctionConfId);
            return new DummyUtilityFunction();
        }
    }
}
