package de.uniks.vs.jalica.unknown;

/**
 * Created by alex on 13.07.17.
 */
public interface IRoleAssignment {


    public void tick();

    Role getOwnRole();

    Role getRole(int robotID);
}
