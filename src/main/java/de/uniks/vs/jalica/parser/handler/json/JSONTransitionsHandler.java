package de.uniks.vs.jalica.parser.handler.json;

import de.uniks.vs.jalica.unknown.ModelFactory;
import de.uniks.vs.jalica.unknown.Plan;
import de.uniks.vs.jalica.unknown.Transition;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;

public class JSONTransitionsHandler extends JSONHandler {

    public static final String TRANSITIONS = "transitions";

    @Override
    public boolean handleIt(Object obj, Plan plan, ModelFactory modelFactory) {
        HashMap.Entry entry = (HashMap.Entry) obj;

        if (TRANSITIONS.equals(entry.getKey())) {
            JSONArray value = (JSONArray) entry.getValue();

            for (Object jsonObject : value ) {
              Transition tran = modelFactory.createTransition((JSONObject)jsonObject, plan);
                plan.getTransitions().add(tran);
            }
            return true;
        }
        return false;
    }
}
