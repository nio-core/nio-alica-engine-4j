package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.model.Role;

import java.util.HashMap;

/**
 * Created by alex on 13.07.17.
 * Updated 23.6.19
 */
public abstract class IRoleAssignment {

    protected Role ownRole;
    protected HashMap<Long,  Role> robotRoleMapping;
    protected IAlicaCommunication communication;

    public IRoleAssignment() {
        this.ownRole = null;
        this.communication = null;
        this.robotRoleMapping = new HashMap<>();
    }

    abstract public void init();
    abstract public void tick();
    abstract public void update();

    public void setCommunication(IAlicaCommunication communication) {
        this.communication = communication;
    }

    public Role getOwnRole() {
        return this.ownRole;
    }

    public Role getRole(long agentID) {
        if (this.robotRoleMapping.containsKey(agentID)) {
                return this.robotRoleMapping.get(agentID);
    } else {
        String ss = "";
        ss += "RA: There is no role assigned for robot: " + agentID + "\n";
        CommonUtils.aboutWarning(ss);
        return null;
    }
    }
}
