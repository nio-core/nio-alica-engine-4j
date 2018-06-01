package de.uniks.vs.jalica.utilfunctions;

import de.uniks.vs.jalica.unknown.BasicConstraint;
import de.uniks.vs.jalica.unknown.BasicUtilityFunction;
import de.uniks.vs.jalica.unknown.CommonUtils;
import de.uniks.vs.jalica.unknown.UtilityFunction1482486227468;

/**
 * Created by alex on 13.07.17.
 */
public class UtilityFunctionCreator {

    public UtilityFunctionCreator() { }

    public BasicUtilityFunction createUtility(long utilityfunctionConfId) {
//        switch ((int) utilityfunctionConfId) {
        if  ( utilityfunctionConfId == 1482486227468L) {
//            case 1482486227468:
            //case 148248622:
            return new UtilityFunction1482486227468();
//            default:
        } else {
                System.out.println("UtilityFunctionCreator: Unknown utility requested: " + utilityfunctionConfId );
                CommonUtils.aboutError("");
//                break;
        }

        return null;
    }
}
