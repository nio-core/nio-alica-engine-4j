package de.uniks.vs.jalica.supplementary;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by alex on 13.07.17.
 */
public class SystemConfig {
    private static SystemConfig instance;

    //    static mutex configsMapMutex;
    static String DOMAIN_FOLDER = "DOMAIN_FOLDER";
    static String DOMAIN_CONFIG_FOLDER = "DOMAIN_CONFIG_FOLDER";
    protected char NODE_NAME_SEPERATOR = '_';
    protected  LinkedHashMap<String, GlobalConfiguration> configs = new LinkedHashMap<>();

    HashMap<String, HashMap<String,String>> map = new HashMap<>();
    protected String rootPath;
    protected String logPath;
    protected String configPath;
    protected String hostname;

    public SystemConfig() {
        HashMap<String,String> alicaConfig = new HashMap<>();
        alicaConfig.put("Alica.SilentStart", "false");
        alicaConfig.put("Alica.UseStaticRoles", "false");
        alicaConfig.put("Alica.MaxEpsPerPlan", "10");
        alicaConfig.put("Alica.AllowIdling", "false");
        alicaConfig.put("Alica.EngineFrequency", "1.0");
        alicaConfig.put("Alica.MinBroadcastFrequency", "0.5");
        alicaConfig.put("Alica.MaxBroadcastFrequency", "0.7");
        alicaConfig.put("Alica.PlanDir", "plans/");
        alicaConfig.put("Alica.RoleDir", "roles/");
        alicaConfig.put("Alica.TeamTimeOut", "1000");
        alicaConfig.put("Alica.TeamBlackList.InitiallyFull", "true");
        alicaConfig.put("Alica.MaxRuleApplications", "1");
        alicaConfig.put("Alica.Team", "1");

        map.put("Alica", alicaConfig);
        configs.put("Globals", new GlobalConfiguration());

        //TODO : path
        configPath = "config/";
        rootPath = ".";
        hostname = "NIO.ZERO";
        logPath = "log/";

       System.out.println("SC: Root:           \"" + rootPath + "\"" );
       System.out.println("SC: ConfigRoot:     \"" + configPath + "\"" );
       System.out.println("SC: Hostname:       \"" + hostname + "\"" );
       System.out.println("SC: Logging Folder: \"" + logPath + "\"" );
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

    public GlobalConfiguration getG(String key) {
        return configs.get(key);
    }

    public String getHostname() {
        return hostname;
    }
}
