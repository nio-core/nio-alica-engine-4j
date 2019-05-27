package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.engine.AlicaCommunication;
import de.uniks.vs.jalica.engine.model.Role;

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
