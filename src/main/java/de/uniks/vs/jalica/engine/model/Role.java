package de.uniks.vs.jalica.engine.model;

import de.uniks.vs.jalica.engine.planselection.RoleTaskMapping;

import java.util.HashMap;

/**
 * Created by alex on 13.07.17.
 */
public class Role extends AlicaElement {
    private HashMap<String, Characteristic> characteristics = new HashMap<>();
    RoleTaskMapping roleTaskMapping;
	RoleDefinitionSet roleDefinitionSet;

    public HashMap<String, Characteristic> getCharacteristics() {
        return characteristics;
    }

    public void setRoleDefinitionSet(RoleDefinitionSet roleDefinitionSet) {
        this.roleDefinitionSet = roleDefinitionSet;
    }

    public void setCharacteristics(HashMap<String, Characteristic> characteristics) {
        this.characteristics = characteristics;
    }

    public RoleTaskMapping getRoleTaskMapping() {
        return roleTaskMapping;
    }

    public void setRoleTaskMapping(RoleTaskMapping roleTaskMapping) {
        this.roleTaskMapping = roleTaskMapping;
    }

    public RoleDefinitionSet getRoleDefinitionSet() {
        return roleDefinitionSet;
    }
}
