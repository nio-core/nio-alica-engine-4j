package de.uniks.vs.jalica.engine.model;

import de.uniks.vs.jalica.engine.common.RoleTaskMapping;

import java.util.ArrayList;

/**
 * Created by alex on 13.07.17.
 */
public class RoleSet extends AlicaElement {

    ArrayList<RoleTaskMapping> roleTaskMappings = new ArrayList<>();
    boolean isDefault;
    /**
     * the plan ID this roleset is defined for
     */
    long usableWithPlanID;

    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public void setUsableWithPlanId(long usableWithPlanID) {
        this.usableWithPlanID = usableWithPlanID;
    }

    public ArrayList<RoleTaskMapping> getRoleTaskMappings() {
        return roleTaskMappings;
    }
}

