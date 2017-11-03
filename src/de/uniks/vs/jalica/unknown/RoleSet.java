package de.uniks.vs.jalica.unknown;

import java.util.ArrayList;

/**
 * Created by alex on 13.07.17.
 */
public class RoleSet extends AlicaElement{

    ArrayList<RoleTaskMapping> roleTaskMappings;
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

