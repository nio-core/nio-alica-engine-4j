package de.uniks.vs.jalica.engine.modelmanagement.parser.handler.json;

import de.uniks.vs.jalica.engine.modelmanagement.ModelFactory;
import de.uniks.vs.jalica.engine.model.Plan;
import de.uniks.vs.jalica.engine.common.SyncTransition;
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
