package de.uniks.vs.jalica.parser.handler.json;

import de.uniks.vs.jalica.parser.handler.json.JSONHandler;
import de.uniks.vs.jalica.unknown.EntryPoint;
import de.uniks.vs.jalica.unknown.ModelFactory;
import de.uniks.vs.jalica.unknown.Plan;
import de.uniks.vs.jalica.unknown.Variable;
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
