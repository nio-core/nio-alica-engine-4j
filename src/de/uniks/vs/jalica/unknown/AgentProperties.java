package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.common.config.ConfigPair;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.supplementary.SystemConfig;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by alex on 14.07.17.
 */
public class AgentProperties {
    private int id = -1;
    private HashMap<String, Characteristic> characteristics;
    private AlicaEngine ae;
    private String name;
    private String defaultRole;
    private HashMap<Long, Capability> capabilities;


    public AgentProperties(AlicaEngine ae, String name) {
        this.ae = ae;
        this.name = name;
        SystemConfig sc = ae.getSystemConfig();
//        this.id = (sc)["Globals"].tryGet<int>(-1, "Globals", "Team", name, "ID", null);
        this.id = Integer.valueOf((String) sc.get("Globals").get("Team."+name+".ID"));
        this.characteristics = new HashMap<>();
        this.capabilities = ae.getPlanRepository().getCapabilities();
        String key = "";
        String kvalue = "";
//        Vector<String> caps = (sc)["Globals"].getNames("Globals", "Team", this.name, null);
        Vector<String> caps = new Vector<>(((ConfigPair)sc.get("Globals").get("Team."+name)).getKeys());
        for (String s : caps)
        {
            if (s.equals("ID") || s.equals("DefaultRole"))
            {
                continue;
            }
            key = s;
//            kvalue = (sc)["Globals"].get<String>("Globals", "Team", this.name, s, null);
            kvalue = (String) sc.get("Globals").get("Team."+name+"."+s);
            for ( Capability capability : this.capabilities.values())
            {
                if (capability.getName().equals(key))
                {
                    for (CapValue val : capability.getCapValues())
                    {
                        //transform(kvalue.begin(), kvalue.end(), kvalue.begin(), ::tolower);
                        if (val.getName().equals(kvalue))
                        {
                            Characteristic cha = new Characteristic();
                            cha.setCapability(capability);
                            cha.setCapValue(val);
                            this.characteristics.put(key, cha);
                        }
                    }
                }

            }
        }
//        this.defaultRole = (*sc)["Globals"].tryGet<string>("NOROLESPECIFIED", "Globals", "Team", name, "DefaultRole",  null);
        this.defaultRole = (String) sc.get("Globals").get("Team."+name+".DefaultRole");
    }

    public int getID() {
        return id;
    }

    public HashMap<String, Characteristic> getCharacteristics() {
        return characteristics;
    }

    public String getName() {
        return name;
    }

}
