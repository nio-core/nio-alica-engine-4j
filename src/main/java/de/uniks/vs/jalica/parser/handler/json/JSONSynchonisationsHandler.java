package de.uniks.vs.jalica.parser.handler.json;

import de.uniks.vs.jalica.parser.handler.json.JSONHandler;
import de.uniks.vs.jalica.unknown.ModelFactory;
import de.uniks.vs.jalica.unknown.Plan;
import de.uniks.vs.jalica.unknown.SyncTransition;
import de.uniks.vs.jalica.unknown.Transition;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;

public class JSONSynchonisationsHandler extends JSONHandler {

    public static final String SYNCHRONISATIONS = "synchronisations";

    @Override
    public boolean handleIt(Object obj, Plan plan, ModelFactory modelFactory) {
        HashMap.Entry entry = (HashMap.Entry) obj;

        if (SYNCHRONISATIONS.equals(entry.getKey())) {
            JSONArray value = (JSONArray) entry.getValue();

            for (Object jsonObject : value ) {
                SyncTransition st = modelFactory.createSyncTransition((JSONObject)jsonObject);
                st.setPlan(plan);
                plan.getSyncTransitions().add(st);
            }
            return true;
        }
        return false;
    }
}
