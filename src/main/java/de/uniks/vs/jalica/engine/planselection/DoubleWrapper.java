package de.uniks.vs.jalica.engine.planselection;

public class DoubleWrapper {

    public double value = 0;

    public DoubleWrapper(double value) { this.value = value; }

    @Override
    public String toString() { return String.valueOf(this.value); }
}
