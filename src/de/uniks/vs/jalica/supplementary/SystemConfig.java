package de.uniks.vs.jalica.supplementary;

import java.util.HashMap;

/**
 * Created by alex on 13.07.17.
 */
public class SystemConfig {

    private static SystemConfig instance;
    private  HashMap<String, Configuration> configMap = new HashMap<>();
    HashMap<String, HashMap<String,String>> map = new HashMap<>();
    private String configPath;

    public SystemConfig() {
        HashMap<String,String> alicaConfig = new HashMap<>();
        alicaConfig.put("Alica.SilentStart", "false");
        alicaConfig.put("Alica.UseStaticRoles", "false");
        alicaConfig.put("Alica.MaxEpsPerPlan", "10");
        alicaConfig.put("Alica.AllowIdling", "false");
        alicaConfig.put("Alica.EngineFrequency", "1.0");
        alicaConfig.put("Alica.MinBroadcastFrequency", "0.5");
        alicaConfig.put("Alica.MaxBroadcastFrequency", "0.7");
        alicaConfig.put("Alica.PlanDir", ".");
        alicaConfig.put("Alica.RoleDir", ".");
        alicaConfig.put("Alica.TeamTimeOut", "1000");
        alicaConfig.put("Alica.TeamBlackList.InitiallyFull", "true");

        map.put("Alica", alicaConfig);

        configMap.put("Globals", new Configuration());

        //TODO : path
        configPath = ".";
    }

    public HashMap<String, String> get(String name) {
        return map.get(name);
    }

    public static SystemConfig getInstance() {

        if (instance == null)
            instance = new SystemConfig();

        return instance;
    }

    public String getConfigPath() {
        return configPath;
    }

    public Configuration getG(String key) {
        return configMap.get(key);
    }
}
