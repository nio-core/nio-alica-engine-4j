package de.uniks.vs.jalica.engine.model;

import de.uniks.vs.jalica.engine.common.RoleTaskMapping;

import java.util.ArrayList;

/**
 * Created by alex on 13.07.17.
 */
public class RoleSet extends AlicaElement {

    private ArrayList<RoleTaskMapping> roleTaskMappings = new ArrayList<>();
    private double defaultPriority;
    /**
     * the plan ID this roleset is defined for
     */
    private long usableWithPlanID;
    private boolean isDefault;

    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public void setUsableWithPlanId(long usableWithPlanID) {
        this.usableWithPlanID = usableWithPlanID;
    }

    public ArrayList<RoleTaskMapping> getRoleTaskMappings() {
        return roleTaskMappings;
    }

    public double getDefaultPriority() {
        return defaultPriority;
    }
    public void setDefaultPriority(double defaultPriority) {
        this.defaultPriority = defaultPriority;
    }
}

