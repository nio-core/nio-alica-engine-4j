package de.uniks.vs.jalica.engine.collections;

import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.model.ForallAgents;
import de.uniks.vs.jalica.engine.model.Quantifier;
import de.uniks.vs.jalica.engine.model.Role;
import de.uniks.vs.jalica.engine.model.Variable;
import de.uniks.vs.jalica.common.utils.CommonUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by alex on 14.07.17.
 */
public class AgentEngineData {

    private AlicaEngine ae;
    /**
     * The robot's AgentProperties
     */
    private AgentProperties properties;
    /**
     * Whether or not the robot is considered active
     */
    private boolean active;
    /**
     * The SuccessMarks of the robot, indicating which EntryPoints it completed.
     */
    private SuccessMarks successMarks;
    /**
     * The timestamp of the last message event from this robot
     */
    double lastMessageTime;

    HashMap<String, Variable> domainVariables = new HashMap<>();
    Role lastRole;


    public AgentEngineData(AlicaEngine ae, AgentProperties properties) {
        this.ae = ae;
        this.active = false;
        this.lastMessageTime = 0;
        this.properties = properties;
        this.initDomainVariables();
        this.successMarks = new SuccessMarks(ae);
        this.lastRole = null;
    }

    public boolean isActive() {
        if(CommonUtils.AED_DEBUG_debug) System.out.println("AED: " + this.properties.getName() +" is " +this.active);
        return active;
    }

    public void setActive(boolean active) {
        if(CommonUtils.AED_DEBUG_debug) System.out.println("AED: " + this.properties.getName() +"("+this.active+") "+" set " + active);
        this.active = active;
    }

    public AgentProperties getProperties() {
        return properties;
    }

    public SuccessMarks getSuccessMarks() {
        return successMarks;
    }

    public void setSuccessMarks(SuccessMarks successMarks) {
        this.successMarks = successMarks;
    }

    public void setLastMessageTime(double lastMessageTime) {
        if(CommonUtils.AED_DEBUG_debug) System.out.println(this.lastMessageTime +" "+ lastMessageTime);
        this.lastMessageTime = lastMessageTime;
    }

    public void initDomainVariables()
    {
        LinkedHashMap<Long, Quantifier> qs = ae.getPlanRepository().getQuantifiers();
        //for (map<long, Quantifier*>::const_iterator iter = qs.begin(); iter != qs.end(); iter++)
        for (Long key : qs.keySet())
        {
            if (qs.get(key) instanceof ForallAgents)
            {
                for (String s : qs.get(key).getDomainIdentifiers())
                {
                    Variable v = new Variable(makeUniqueId(s), this.getProperties().getName() + "." + s,"");
                    this.domainVariables.put(s,v);
                }
            }
        }
    }

    public Variable getDomainVariable(String ident)
    {
        return this.domainVariables.get(ident);
//        if(iterator != this.domainVariables.end())
//        {
//            return iterator.second;
//        }
//		else
//        {
////			cout << "AgentEngineData: DomainVarible not found returning nullptr" << endl;
//            return nullptr;
//        }
    }


    public long makeUniqueId(String s)
    {
        long ret = (long)this.getProperties().extractID() << 32;
        ret +=  s.hashCode();

        if(this.ae.getPlanParser().getParsedElements().get(ret) != null) {
            ae.abort("TO: Hash Collision in generating unique ID: ", ""+ret);
        }

//        iterator = this.ae.getPlanParser().getParsedElements().find(ret);
//        if(iterator != ae.getPlanParser().getParsedElements().end())
//        {
//            ae.abort("TO: Hash Collision in generating unique ID: ", ""+ret);
//        }
        return ret;
    }

    public Role getLastRole() {
        return lastRole;
    }

    public void setLastRole(Role lastRole) {
        this.lastRole = lastRole;
    }

    public double getLastMessageTime() { return lastMessageTime; }
}
