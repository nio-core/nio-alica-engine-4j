package de.uniks.vs.jalica.engine.modelmanagement.parser.handler.json;

import de.uniks.vs.jalica.engine.modelmanagement.ModelFactory;
import de.uniks.vs.jalica.engine.model.Plan;
import de.uniks.vs.jalica.engine.model.Transition;
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
              Transition transition = modelFactory.createTransition((JSONObject)jsonObject, plan);
                plan.getTransitions().add(transition);
            }
            return true;
        }
        return false;
    }
}
