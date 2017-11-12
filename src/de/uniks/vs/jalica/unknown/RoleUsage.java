package de.uniks.vs.jalica.unknown;

/**
 * Created by alex on 10.11.17.
 */
public class RoleUsage {
    int priorityOrder;
    Role role;
    boolean used;

    public RoleUsage(int priorityOrder, Role role) {
        this.priorityOrder = priorityOrder;
        this.role = role;
        this.used = false;
    }

    public Role getRole() {
        return role;
    }
}
