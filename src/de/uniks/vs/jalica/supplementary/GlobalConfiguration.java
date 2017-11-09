package de.uniks.vs.jalica.supplementary;

import java.util.HashMap;

/**
 * Created by alex on 14.07.17.
 */
public class GlobalConfiguration {

    HashMap<String, HashMap<String, HashMap<String, String>>> configValues;

    public GlobalConfiguration() {
        this.configValues = new HashMap<>();

        HashMap<String, String> nio = new HashMap<>();
        nio.put("ID","42");
        nio.put("DefaultRole","Defender");
        nio.put("AverageTranslation","3000.0");
        nio.put("MaxTranslation","3000.0");
        nio.put("AverageRotation","3.1415");
        nio.put("IsOmnidrive","True");
        nio.put("Kicker","True");
        nio.put("IsGoalie","False");
        nio.put("Speed","Medium");
        nio.put("CanPass","True");
        nio.put("LocalizationSuccess","-1");
        nio.put("HasActiveBallHandler","True");

        HashMap<String, HashMap<String, String>> team = new HashMap<>();
        team.put("NIO.ZERO", nio);


        this.configValues.put("Team", team);
    }

    public HashMap<String, HashMap<String, String>> getSections(String section) {
        return this.configValues.get(section);
    }

//    public class ConfVector<T> extends Vector {
//
//        public T get(T id) {
//
//            for (Object i: elementData) {
//
//                if (id.equals(i))
//                    return (T)i;
//            }
//
//            return null;
//        }
//    }
}

