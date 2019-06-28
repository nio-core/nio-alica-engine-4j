package de.uniks.vs.jalica.engine.collections;

import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.idmanagement.IDManager;
import de.uniks.vs.jalica.engine.model.*;
import de.uniks.vs.jalica.common.utils.CommonUtils;

import java.util.*;

/**
 * Created by alex on 14.07.17.
 */
public class AgentEngineData {

    private AlicaEngine engine;
    private long id;
    // indicating which EntryPoints it completed.
    private SuccessMarks successMarks;
    HashMap<Variable, DomainVariable> domainVariables = new HashMap<>();

//    private AgentProperties properties;
//    private Role currentRole;
//    //agent is considered active
//    private boolean active;
//    private double lastMessageTime;

    public AgentEngineData(AlicaEngine engine,  long id) {
        this.engine = engine;
        this.id = id;
        this.successMarks = new SuccessMarks();
        this.initDomainVariables();
    }

    public void updateSuccessMarks( ArrayList succeededEps) {
        this.successMarks.update(this.engine, succeededEps);
    }

    public void clearSuccessMarks() {
        this.successMarks.clear();
    }

    public void initDomainVariables() {
        Set<Map.Entry<Long, Quantifier>> quantifiers = this.engine.getPlanRepository().getQuantifiers().entrySet();

        for (Map.Entry<Long, Quantifier> quantifier : quantifiers) {

            for ( Variable variable : quantifier.getValue().getTemplateVariables()) {
                DomainVariable domainVariable = new DomainVariable(makeUniqueId(variable.getName()), id + "." + variable.getName(), "", variable, id);
                this.domainVariables.put(variable, domainVariable);
            }
        }
    }

    public DomainVariable getDomainVariable(Variable templateVar) {
        return this.domainVariables.get(templateVar);
    }

    public DomainVariable getDomainVariable(String name) {
        CommonUtils.aboutCallNotification("AgentEngineData:getDomainVariable by name " + name);
        Variable tv = this.engine.getPlanRepository().getVariables().get(name);
        return getDomainVariable(tv);

//        return this.domainVariables.get(ident);
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

    private long makeUniqueId( String s)  {
        Long uniqueID = IDManager.generateUniqueID(s);
        assert(!this.engine.getModelManager().idExists(uniqueID));
        return uniqueID;
    }

    public SuccessMarks getSuccessMarks() {
        return successMarks;
    }

    public void setSuccessMarks(SuccessMarks successMarks) {
        this.successMarks = successMarks;
    }



//    public boolean isActive() {
//        if(CommonUtils.AED_DEBUG_debug) System.out.println("AED: " + this.properties.getName() +" is " +this.active);
//        return active;
//    }
//
//    public void setActive(boolean active) {
//        if(CommonUtils.AED_DEBUG_debug) System.out.println("AED: " + this.properties.getName() +"("+this.active+") "+" set " + active);
//        this.active = active;
//    }
//
//    public AgentProperties getProperties() {
//        return properties;
//    }
//
//    public void setLastMessageTime(double lastMessageTime) {
//        if(CommonUtils.AED_DEBUG_debug) System.out.println(this.lastMessageTime +" "+ lastMessageTime);
//        this.lastMessageTime = lastMessageTime;
//    }
//
//    public long makeUniqueId(String s)
//    {
//        long ret = (long)this.getProperties().extractID() << 32;
//        ret +=  s.hashCode();
//
//        if(this.ae.getPlanParser().getParsedElements().get(ret) != null) {
//            ae.abort("TO: Hash Collision in generating unique ID: ", ""+ret);
//        }
//
////        iterator = this.ae.getPlanParser().getParsedElements().find(ret);
////        if(iterator != ae.getPlanParser().getParsedElements().end())
////        {
////            ae.abort("TO: Hash Collision in generating unique ID: ", ""+ret);
////        }
//        return ret;
//    }
//
//    public Role getCurrentRole() {
//        return currentRole;
//    }
//
//    public void setCurrentRole(Role currentRole) {
//        this.currentRole = currentRole;
//    }
//
//    public double getLastMessageTime() { return lastMessageTime; }
}
