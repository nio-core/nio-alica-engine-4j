package de.uniks.vs.jalica.utilfunctions;

import de.uniks.vs.jalica.behaviours.helloworld.testcommunication1528124991817.UtilityFunction1528124991817;
import de.uniks.vs.jalica.behaviours.helloworld.testcommunicationmaster1528124971225.UtilityFunction1528124971225;
import de.uniks.vs.jalica.unknown.BasicConstraint;
import de.uniks.vs.jalica.unknown.BasicUtilityFunction;
import de.uniks.vs.jalica.unknown.CommonUtils;
import de.uniks.vs.jalica.unknown.UtilityFunction1482486227468;

/**
 * Created by alex on 13.07.17.
 */
public class UtilityFunctionCreator {

    public UtilityFunctionCreator() {
    }

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
        else {
            System.out.println("UtilityFunctionCreator: Unknown utility requested: " + utilityfunctionConfId);
            CommonUtils.aboutError("");
            return null;
        }
    }
}
