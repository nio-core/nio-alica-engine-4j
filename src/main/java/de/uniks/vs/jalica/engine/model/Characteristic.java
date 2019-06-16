package de.uniks.vs.jalica.engine.model;

import de.uniks.vs.jalica.common.utils.CommonMetrics;
import de.uniks.vs.jalica.common.utils.CommonUtils;

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

    public float calculateSimilarityTo(Characteristic characteristic) {
        float similarity = 1.0f -
                        (CommonMetrics.levenshteinDistance(this.name, characteristic.getName())
                                / (this.name.length() > characteristic.getName().length() ? this.name.length() : characteristic.getName().length()));

        if (CommonUtils.isNumeric(this.value) ^ CommonUtils.isNumeric(characteristic.getValue())) {
            similarity = 0;
        }
        else {
            similarity += 1.0f -
                    (CommonMetrics.levenshteinDistance(this.value, characteristic.getValue())
                            / (this.value.length() > characteristic.getValue().length() ? this.value.length() : characteristic.getValue().length()));
            similarity/=2;
        }

        if (CommonUtils.C_DEBUG_debug) System.out.println("C: " + CommonUtils.isNumeric(this.value) +" " + CommonUtils.isNumeric(characteristic.getValue()));
        if (CommonUtils.C_DEBUG_debug) System.out.println("C: " + this.name + ":" + this.value + "     " + characteristic.getName() + ":" + characteristic.getValue() +   "     " + similarity );

        return similarity;
    }

//    public double similarityValue(String value1, String value2) {
//        System.out.println("C: similarity of " + value1 + " and " + value2);
//        return 0;
//    }

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
