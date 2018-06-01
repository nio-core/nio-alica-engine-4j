package de.uniks.vs.jalica.supplementary;

import de.uniks.vs.jalica.common.config.ConfigPair;
import de.uniks.vs.jalica.common.config.ConfigParser;

import java.util.HashMap;

/**
 * Created by alex on 13.07.17.
 */
public class SystemConfig {
    //    static mutex configsMapMutex;
    static String DOMAIN_FOLDER = "DOMAIN_FOLDER";
    static String DOMAIN_CONFIG_FOLDER = "DOMAIN_CONFIG_FOLDER";
    protected char NODE_NAME_SEPERATOR = '_';

    protected String rootPath;
    protected String logPath;
    protected String configPath;
    protected String id= "nio_zero";

    private HashMap<String, ConfigPair> configurations;

    public SystemConfig() {
        init();
    }

    public SystemConfig(String id) {
        this.id = id;
        init();
    }

    private void init() {
        configurations = new HashMap<>();
        //TODO : path
        configPath = "config/";
        rootPath = ".";
        logPath = "log/";

        System.out.println("SC: Root:           \"" + rootPath + "\"" );
        System.out.println("SC: ConfigRoot:     \"" + configPath + "\"" );
        System.out.println("SC: Hostname:       \"" + id + "\"" );
        System.out.println("SC: Logging Folder: \"" + logPath + "\"" );
    }

    public ConfigPair get(String name) {
        ConfigPair config = getConfigOf(name);
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

    public String getConfigPath() {
        return configPath;
    }

    public String getHostname() {return id;}

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
        return Integer.valueOf((String) this.get("Globals").get("Team." + name +".ID"));
    }
}
