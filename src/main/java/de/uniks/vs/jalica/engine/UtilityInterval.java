package de.uniks.vs.jalica.engine;

/**
 * Created by alex on 31.07.17.
 */
public class UtilityInterval {
    private  double min;
    private  double max;

    public UtilityInterval(double min, double max) {

        this.min = min;
        this.max = max;
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public void setMin(double min) {
        this.min = min;
    }
}
