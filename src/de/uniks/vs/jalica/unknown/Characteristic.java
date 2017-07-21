package de.uniks.vs.jalica.unknown;

/**
 * Created by alex on 13.07.17.
 */
public class Characteristic {
    private String name;
    private CapValue capValue;
    private Capability capability;
    private double weight;

    public String getName() {
        return name;
    }

    public CapValue getCapValue() {
        return capValue;
    }

    public Capability getCapability() {
        return capability;
    }

    public double getWeight() {
        return weight;
    }
}
