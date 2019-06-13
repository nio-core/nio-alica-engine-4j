package de.uniks.vs.jalica.engine.common;

import de.uniks.vs.jalica.common.FileSystem;
import de.uniks.vs.jalica.engine.common.config.ConfigPair;
import de.uniks.vs.jalica.engine.common.config.ConfigParser;
import de.uniks.vs.jalica.engine.idmanagement.IDManager;

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
    protected String id;

    private HashMap<String, ConfigPair> configurations;

    public SystemConfig() {
        this.rootPath = "./";
        this.logPath = "log/";
        this.configPath = "config/";
        init();
    }

    public SystemConfig(String id) {
        this.rootPath = "./";
        this.logPath = "log/";
        this.configPath = "config/";
        this.id = id;
        init();
    }

    public SystemConfig(String id, String rootPath, String logPath, String configPath) {
        this.rootPath = rootPath;
        this.logPath = logPath;
        this.configPath = configPath;
        this.id = id;
        init();
    }
    public SystemConfig(String rootPath, String logPath, String configPath) {
        this.rootPath = rootPath;
        this.logPath = logPath;
        this.configPath = configPath;
        init();
    }

    private void init() {
        configurations = new HashMap<>();
        //TODO : path
//        configPath = confPath;//"config/";
//        rootPath = rootPath; //".";
//        logPath = logPath; //"log/";

        System.out.println("SysC: Root:           \"" + rootPath + "\"" );
        System.out.println("SysC: Config Path:     \"" + configPath + "\"" );
        System.out.println("SysC: Hostname:       \"" + id + "\"" );
        System.out.println("SysC: Logging Folder: \"" + logPath + "\"" );
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
        String path = FileSystem.findFile(rootPath + configPath , name);
        return ConfigParser.getInstance().parse(path);
    }

    public String getConfigPath() {
        return configPath;
    }

    public String getHostname() {return id;}

    /**
     * Looks up the own agent's ID with the system config's local hostname.
     * @return The own agent's ID
     */
    public long getOwnAgentID() {
        return this.getAgentID(this.getHostname());
    }

    /**
     * Looks up the agent's ID with the given name.
     * @return The agent's ID
     */
    long getAgentID(String name) {
        Object id = this.get("Globals").get("Team." + name + ".ID");

        if (id != null)
            return Integer.valueOf((String) this.get("Globals").get("Team." + name +".ID"));
        else
            return IDManager.generateUUID(name).asLong();
    }

    public String getRootPath() {
        return rootPath;
    }
}
