package de.uniks.vs.jalica.engine.common;

import de.uniks.vs.jalica.common.utils.CommonUtils;

/**
 * Created by alex on 31.07.17.
 */
public class DynCardinality {
    private int min;
    private int max;

    public DynCardinality() {
        min = -1;
        max = -1;
    }

    public DynCardinality(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public void setMin(int min) {
        if (CommonUtils.DC_debug) CommonUtils.aboutCalledFrom("Min Card: " + this.min + " -> " + min);
        this.min = min;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public int getMax() { return max; }
}
