package de.uniks.vs.jalica.unknown;

/**
 * Created by alex on 13.07.17.
 */
public interface IRoleAssignment {

    void init();
    void tick();
    void update();

    Role getOwnRole();
    Role getRole(long agentID);
    void setCommunication(AlicaCommunication communication);
}
