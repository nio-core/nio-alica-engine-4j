package de.uniks.vs.jalica.communication;

import de.uniks.vs.jalica.engine.AlicaEngine;

import java.util.HashMap;

public class MessageTopics {

    public enum Type {
        allocationAuthorityInfoTopic,
        ownRoleTopic,
        planTreeInfoTopic,
        solverResultTopic,
        syncReadyTopic,
        syncTalkTopic,
        alicaEngineInfoTopic,
        discoveryTopic
    };

    private String configFile;
    private AlicaEngine alicaEngine;
    private HashMap<Type, String> topics = new HashMap();

    public MessageTopics(AlicaEngine alicaEngine, String configfile) {
        this.alicaEngine = alicaEngine;
        this.configFile = configfile;
    }

    public MessageTopics loadTopics() {

        for (Type topic : Type.values()) {
            topics.put(topic, (String) this.alicaEngine.getSystemConfig().get(configFile).get("Topics." + topic.name()));
        }
        return this;
    }

    public String getTopic(Type topic) {
        return topics.get(topic);
    }
}


//    private String alicaEngineInfoTopic;
//    private String allocationAuthorityInfoTopic;
//    private String ownRoleTopic;
//    private String planTreeInfoTopic;
//    private String syncReadyTopic;
//    private String syncTalkTopic;
//    private String solverResultTopic;

//        this.allocationAuthorityInfoTopic = (String) systemConfig.get(configFile).get("Topics.allocationAuthorityInfoTopic");
//        this.ownRoleTopic = (String) systemConfig.get(configFile).get("Topics.ownRoleTopic");
//        this.alicaEngineInfoTopic = (String) systemConfig.get(configFile).get("Topics.alicaEngineInfoTopic");
//        this.planTreeInfoTopic = (String) systemConfig.get(configFile).get("Topics.planTreeInfoTopic");
//        this.syncReadyTopic = (String) this.systemConfig.get("AlicaRosProxy").get("Topics.syncReadyTopic");
//        this.syncTalkTopic = (String) this.systemConfig.get("AlicaRosProxy").get("Topics.syncTalkTopic");
//        this.solverResultTopic = (String) systemConfig.get(configFile).get("Topics.solverResultTopic");