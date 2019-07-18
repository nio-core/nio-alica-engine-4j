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

    public void sumWith(double weight, UtilityInterval interval) {
        this.setMax(this.getMax() + weight * interval.getMax());
        this.setMin(this.getMin() + weight * interval.getMin());
    }

    public void devideBy(double number) {
        this.setMax(this.getMax() / number);
        this.setMin(this.getMin() / number);
    }

    public double size() {
        return max - min;
    }

    @Override
    public String toString() {
        return"[" + this.getMin() + ", " + this.getMax() + "]";
    }
}
