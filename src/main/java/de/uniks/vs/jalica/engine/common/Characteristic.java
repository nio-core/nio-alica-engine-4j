package de.uniks.vs.jalica.engine.common;

import de.uniks.vs.jalica.engine.model.AlicaElement;

/**
 * Created by alex on 13.07.17.
 */
public class Characteristic extends AlicaElement {

    protected String value;
    protected double weight;

    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }

    public double getWeight() {
        return weight;
    }
    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double similarityValue(String value1, String value2) {
        return 0;
    }

//    private Capability capability;
//    private CapValue capValue;
//
//    public CapValue getCapValue() {
//        return capValue;
//    }
//
//    public Capability getCapability() {
//        return capability;
//    }
//
//    public void setCapability(Capability capability) {
//        this.capability = capability;
//    }
//
//    public void setCapValue(CapValue capValue) {
//        this.capValue = capValue;
//    }
}
