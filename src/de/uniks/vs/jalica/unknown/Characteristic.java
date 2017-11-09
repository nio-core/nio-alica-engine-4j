package de.uniks.vs.jalica.unknown;

/**
 * Created by alex on 13.07.17.
 */
public class Characteristic extends AlicaElement {
    private CapValue capValue;
    private Capability capability;
    private double weight;

    public CapValue getCapValue() {
        return capValue;
    }

    public Capability getCapability() {
        return capability;
    }

    public double getWeight() {
        return weight;
    }

    public void setCapability(Capability capability) {
        this.capability = capability;
    }

    public void setCapValue(CapValue capValue) {
        this.capValue = capValue;
    }

    public void setWeight(double weight) {

        this.weight = weight;
    }
}
