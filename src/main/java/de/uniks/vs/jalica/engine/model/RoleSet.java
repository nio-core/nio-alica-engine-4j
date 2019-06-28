package de.uniks.vs.jalica.engine.model;

import de.uniks.vs.jalica.engine.planselection.RoleTaskMapping;

import java.util.ArrayList;

/**
 * Created by alex on 13.07.17.
 * Updated 23.6.19
 */
public class RoleSet extends AlicaElement {

    private ArrayList<RoleTaskMapping> roleTaskMappings = new ArrayList<>();
    private double defaultPriority;
    /**
     * the plan ID this roleset is defined for
     */
    private long usableWithPlanID;
    private boolean isDefault;
    private String fileName;

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

    String getFileName()  { return this.fileName; }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ArrayList<Role> getRoles() {
        ArrayList<Role> roles = new ArrayList<>();

        for (RoleTaskMapping mapping: this.roleTaskMappings) {
            roles.add(mapping.getRole());
        }
        return roles;
    }

    @Override
    public String toString() {
        String indent = "";
        String ss = "";
        ss += indent + "#RoleSet: " + getName() + " " + this.getID() + "\n";
        ss += indent + "\t UsableWithPlanID: " + this.usableWithPlanID + "\n";
        ArrayList<Role> roles = getRoles();
        ss += indent + "\t Contains Mappings: " + roles.size() + "\n";

        for ( Role role : roles) {
            ss += role.toString();
        }
        ss += "#EndRoleSet" + "\n";
        return ss;
    }
}

