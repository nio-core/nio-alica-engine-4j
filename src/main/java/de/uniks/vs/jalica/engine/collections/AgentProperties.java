package de.uniks.vs.jalica.engine.collections;

import de.uniks.vs.jalica.engine.common.config.ConfigPair;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.common.SystemConfig;
import de.uniks.vs.jalica.engine.common.Capability;
import de.uniks.vs.jalica.engine.common.Characteristic;
import de.uniks.vs.jalica.engine.idmanagement.IDManager;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by alex on 14.07.17.
 */
public class AgentProperties {
    private long id;
    private String name;
    private AlicaEngine ae;
    private String defaultRole;
    private HashMap<String, Characteristic> characteristics;
//    private HashMap<Long, Capability> capabilities;

    public AgentProperties(AlicaEngine ae, String name) {
        this.ae = ae;
        this.name = name;
        this.id = extractID(this.name, this.ae.getSystemConfig());
//        this.capabilities = ae.getPlanRepository().getCapabilities();
        this.characteristics = extractAgentCharacteristics(this.name, this.ae.getSystemConfig());
        this.defaultRole = extractDefaultRole(this.name, this.ae.getSystemConfig());
    }

    private String extractDefaultRole(String name, SystemConfig sc) {
        Object _defaultRole = sc.get("Globals").get("Team." + name + ".defaultRole") == null ?
                sc.get("Globals").get("Team." + name + ".DefaultRole") : sc.get("Globals").get("Team." + name + ".defaultRole");
        return _defaultRole != null ? (String) _defaultRole : "None";
    }

    private long extractID(String name, SystemConfig sc) {
        Object id = sc.get("Globals").get("Team." + name + ".ID");

        if (id != null)
            return Integer.valueOf((String) id);
        else
           return IDManager.generateUUID(this).asLong();
    }

    private HashMap<String, Characteristic> extractAgentCharacteristics(String name, SystemConfig sc) {
        ArrayList<String> characteristics = new ArrayList<>(((ConfigPair) sc.get("Globals").get("Team." + name)).getKeys());
        HashMap<String, Characteristic> characteristicMap = new HashMap<>();

        for (String key : characteristics) {

            if (key.equals("ID") || key.equals("defaultRole")) {
                continue;
            }
            String value = (String) sc.get("Globals").get("Team." + name + "." + key);
            Characteristic characteristic = new Characteristic();
            characteristic.setName(key);
            characteristic.setValue(value);
            characteristicMap.put(key, characteristic);
        }
        return characteristicMap;
    }

//    private void extractCharacteristicsWithCapabilities(String name, SystemConfig sc) {
//        String key = "";
//        String kvalue = "";
//        Vector<String> caps = new Vector<>(((ConfigPair)sc.get("Globals").get("Team."+name)).getKeys());
//
//        for (String s : caps)
//        {
//            if (s.equals("ID") || s.equals("defaultRole"))
//            {
//                continue;
//            }
//            key = s;
////            kvalue = (systemConfig)["Globals"].get<String>("Globals", "Team", this.name, s, null);
//            kvalue = (String) sc.get("Globals").get("Team."+name+"."+s);
//            for ( Capability capability : this.capabilities.values())
//            {
//                if (capability.getName().equals(key))
//                {
//                    for (CapValue val : capability.getCapValues())
//                    {
//                        if (val.getName().equals(kvalue))
//                        {
//                            Characteristic cha = new Characteristic();
//                            cha.setName(capability.getName());
//                            cha.setValue(val.toString());
//                            this.characteristics.put(key, cha);
//                        }
//                    }
//                }
//
//            }
//        }
//    }

    public long extractID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDefaultRole() { return defaultRole; }

    public HashMap<String, Characteristic> getCharacteristics() {
        return characteristics;
    }
}
