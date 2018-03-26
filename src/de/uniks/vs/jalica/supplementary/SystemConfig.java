package de.uniks.vs.jalica.supplementary;

import de.uniks.vs.jalica.common.ConfigPair;
import de.uniks.vs.jalica.common.ConfigParser;

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
//    protected  LinkedHashMap<String, GlobalConfiguration> configs = new LinkedHashMap<>();

//    HashMap<String, HashMap<String,String>> map = new HashMap<>();
    protected String rootPath;
    protected String logPath;
    protected String configPath;
    protected String hostname;

    private HashMap<String, ConfigPair> configurations;

    public SystemConfig() {
        configurations = new HashMap<>();
//        HashMap<String,String> alicaConfig = new HashMap<>();
//        alicaConfig.put("Alica.SilentStart", "true");
//        alicaConfig.put("Alica.UseStaticRoles", "false");
//        alicaConfig.put("Alica.MaxEpsPerPlan", "10");
//        alicaConfig.put("Alica.AllowIdling", "false");
//        alicaConfig.put("Alica.EngineFrequency", "1.0");
//        alicaConfig.put("Alica.MinBroadcastFrequency", "0.5");
//        alicaConfig.put("Alica.MaxBroadcastFrequency", "0.7");
//        alicaConfig.put("Alica.PlanDir", "plans/");
//        alicaConfig.put("Alica.RoleDir", "roles/");
//        alicaConfig.put("Alica.TeamTimeOut", "1000");
//        alicaConfig.put("Alica.TeamBlackList.InitiallyFull", "true");
//        alicaConfig.put("Alica.MaxRuleApplications", "1");
//        alicaConfig.put("Alica.Team", "1");
//        alicaConfig.put("Alica.AssignmentProtectionTime", "500");
//
//        alicaConfig.put("Alica.CycleDetection.CycleCount", "5");
//        alicaConfig.put("Alica.CycleDetection.Enabled", "true");
//        alicaConfig.put("Alica.CycleDetection.MinimalAuthorityTimeInterval", "800");
//        alicaConfig.put("Alica.CycleDetection.MaximalAuthorityTimeInterval", "500");
//        alicaConfig.put("Alica.CycleDetection.MessageTimeInterval", "60");
//        alicaConfig.put("Alica.CycleDetection.MessageWaitTimeInterval", "200");
//        alicaConfig.put("Alica.CycleDetection.HistorySize", "45");
//        alicaConfig.put("Alica.CycleDetection.IntervalIncreaseFactor", "1.5");
//        alicaConfig.put("Alica.CycleDetection.IntervalDecreaseFactor", "0.999");
//
//        map.put("Alica", alicaConfig);
//        configs.put("Globals", new GlobalConfiguration());

        //TODO : path
        configPath = "config/";
        rootPath = ".";
        hostname = "nio_zero";
        logPath = "log/";

       System.out.println("SC: Root:           \"" + rootPath + "\"" );
       System.out.println("SC: ConfigRoot:     \"" + configPath + "\"" );
       System.out.println("SC: Hostname:       \"" + hostname + "\"" );
       System.out.println("SC: Logging Folder: \"" + logPath + "\"" );
    }

    public ConfigPair get(String name) {
        ConfigPair config = getConfigOf(name);
//        String s = config.get("Alica.SilentStart");
        return config;
    }

    private ConfigPair getConfigOf(String name) {

        if (!configurations.containsKey(name))
            configurations.put(name, loadConfigFromFile(name +".conf"));
        return configurations.get(name);
    }

    private ConfigPair loadConfigFromFile(String name) {
        String path = FileSystem.findFile(configPath, name, null);
        return ConfigParser.getInstance().parse(path);
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
        return null;
//        return configs.get(key);
    }

    public String getHostname() {return hostname;}

    /**
     * Looks up the own robot's ID with the system config's local hostname.
     * @return The own robot's ID
     */
    public int getOwnRobotID() {
        return this.getRobotID(this.getHostname());
    }
    /**
     * Looks up the robot's ID with the given name.
     * @return The robot's ID
     */
    int getRobotID(String name) {
//        return Integer.valueOf(this.getG("Globals").getSections("Team").get(name).get("ID"));
        return Integer.valueOf((String) this.get("Globals").get("Team." + name +".ID"));
    }
}
