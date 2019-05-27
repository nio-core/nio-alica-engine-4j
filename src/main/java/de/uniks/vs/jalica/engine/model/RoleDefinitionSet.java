package de.uniks.vs.jalica.engine.model;

import java.util.ArrayList;

/**
 * Created by alex on 13.07.17.
 */
public class RoleDefinitionSet extends AlicaElement {

    String fileName;
    ArrayList<Role> roles = new ArrayList<>();

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ArrayList<Role> getRoles() {
        return roles;
    }

    public void setRoles(ArrayList<Role> roles) {
        this.roles = roles;
    }
}
