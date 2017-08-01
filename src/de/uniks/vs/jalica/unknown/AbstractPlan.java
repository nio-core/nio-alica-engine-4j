package de.uniks.vs.jalica.unknown;

import com.sun.javafx.geom.CubicCurve2D;
import de.uniks.vs.jalica.common.UtilityFunction;

/**
 * Created by alex on 17.07.17.
 */
public class AbstractPlan extends AlicaElement {
    private PreCondition preCondition;
    private RuntimeCondition runtimeCondition;
    private UtilityFunction utilityFunction;
    private double utilityThreshold;

    public PreCondition getPreCondition() {
        return preCondition;
    }

    public RuntimeCondition getRuntimeCondition() {
        return runtimeCondition;
    }

    public UtilityFunction getUtilityFunction() {
        return utilityFunction;
    }

    public double getUtilityThreshold() {
        return utilityThreshold;
    }
}
