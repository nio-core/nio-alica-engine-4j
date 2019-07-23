package de.uniks.vs.jalica.engine.teammanagement;

import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.AlicaTime;
import de.uniks.vs.jalica.engine.collections.AgentEngineData;
import de.uniks.vs.jalica.engine.collections.AgentProperties;
import de.uniks.vs.jalica.engine.idmanagement.ID;
import de.uniks.vs.jalica.engine.model.AbstractPlan;
import de.uniks.vs.jalica.engine.model.DomainVariable;
import de.uniks.vs.jalica.engine.model.EntryPoint;

import java.util.ArrayList;

/**
 * created 25.6.19
 */

public class Agent {

    AlicaEngine engine;
    ID id;
    boolean active;
    boolean ignored;
    boolean local;
    AlicaTime timeout;
    AlicaTime timeLastMsgReceived;
    AgentProperties properties;
    AgentEngineData engineData;
    String name;

    public Agent(AlicaEngine engine, AlicaTime timeout, ID id) {
        this(engine, timeout, id, "");
    }

    public Agent(AlicaEngine engine, AlicaTime timeout, ID id, String name) {
        this.id = id;
        this.name = name;
        this.engine = engine;
//        this.properties =  !name.isEmpty() ? new AgentProperties(engine, name) : new AgentProperties();
        this.properties = new AgentProperties(engine, name);
        this.engineData = new AgentEngineData(engine, id);
//        this.engineData = new AgentProperties(engine, id);
        this.timeout = timeout;
        this.active = false;
        this.ignored = false;
        this.local = false;

    }

    public ID getId()  { return this.id; }
    public String getName()  { return this.name; }
    public AgentProperties getProperties()  { return this.properties; }
    public AgentEngineData getEngineData()  { return this.engineData; }
    public boolean isActive()  { return this.active; }
    public boolean isIgnored()  { return this.ignored; }


    public void setLocal(boolean local) {
        if (local) {
            this.active = true;
        }
        this.local = local;
    }

    public void setIgnored(boolean ignored) { this.ignored = ignored; }

    public void setTimeLastMsgReceived(AlicaTime timeLastMsgReceived) { this.timeLastMsgReceived = timeLastMsgReceived; }

    public void setSuccess(AbstractPlan plan, EntryPoint entryPoint) {
        this.engineData.getSuccessMarks().markSuccessfull(plan, entryPoint);
    }
    public void setSuccessMarks(ArrayList suceededEps) {
        this.engineData.updateSuccessMarks(suceededEps);
    }

    public DomainVariable getDomainVariable(String sort){
        return this.engineData.getDomainVariable(sort);
    }

    public ArrayList<EntryPoint> getSucceededEntryPoints(AbstractPlan plan) {
        return this.engineData.getSuccessMarks().succeededEntryPoints(plan);
    }

    public boolean update() {
        if (this.local) {
            return false;
        }
        if (this.active && this.timeLastMsgReceived.time + this.timeout.time < this.engine.getAlicaClock().now().time) {
            // timeout triggered
            this.engineData.clearSuccessMarks();
            this.active = false;
            return true;
        }

        if (!this.active && this.timeLastMsgReceived.time + this.timeout.time > this.engine.getAlicaClock().now().time) {
            // reactivate because of new messages
            this.active = true;
            return true;
        }

        return false;
    }

}
