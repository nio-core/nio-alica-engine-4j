package de.uniks.vs.jalica.supplementary;

import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by alex on 13.07.17.
 */
public class SystemConfig {

    private static SystemConfig instance;
    HashMap<String, HashMap<String,String>> map = new HashMap<>();

    public SystemConfig() {
        HashMap<String,String> alicaConfig = new HashMap<>();
        alicaConfig.put("Alica.SilentStart", "false");
        alicaConfig.put("Alica.UseStaticRoles", "false");
        alicaConfig.put("Alica.MaxEpsPerPlan", "10");
        alicaConfig.put("Alica.AllowIdling", "false");

        map.put("Alica", alicaConfig);
    }

    public HashMap<String, String> get(String name) {
        return map.get(name);
    }

    public static SystemConfig getInstance() {

        if (instance == null)
            instance = new SystemConfig();

        return instance;
    }
}
