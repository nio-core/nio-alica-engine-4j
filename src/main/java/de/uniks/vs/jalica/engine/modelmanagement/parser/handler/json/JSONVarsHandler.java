package de.uniks.vs.jalica.engine.modelmanagement.parser.handler.json;

import de.uniks.vs.jalica.engine.modelmanagement.ModelFactory;
import de.uniks.vs.jalica.engine.model.Plan;
import de.uniks.vs.jalica.engine.model.Variable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;

public class JSONVarsHandler extends JSONHandler {

    public static final String VARS = "variables";

    @Override
    public boolean handleIt(Object obj, Plan plan, ModelFactory modelFactory) {
        HashMap.Entry entry = (HashMap.Entry) obj;

        if (VARS.equals(entry.getKey())) {
            JSONArray value = (JSONArray) entry.getValue();

            for (Object jsonObject : value ) {
                Variable var = modelFactory.createVariable((JSONObject)jsonObject);
                plan.getVariables().add(var);
            }
            return true;
        }
        return false;
    }
}
