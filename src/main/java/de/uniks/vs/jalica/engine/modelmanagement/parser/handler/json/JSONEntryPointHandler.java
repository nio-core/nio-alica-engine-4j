package de.uniks.vs.jalica.engine.modelmanagement.parser.handler.json;

import de.uniks.vs.jalica.engine.model.EntryPoint;
import de.uniks.vs.jalica.engine.modelmanagement.ModelFactory;
import de.uniks.vs.jalica.engine.model.Plan;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;

public class JSONEntryPointHandler extends JSONHandler {

    public static final String ENTRY_POINTS = "entryPoints";

    @Override
    public boolean handleIt(Object obj, Plan plan, ModelFactory modelFactory) {

        HashMap.Entry entry = (HashMap.Entry) obj;

        if (ENTRY_POINTS.equals(entry.getKey())) {
            JSONArray entryPoints = (JSONArray) entry.getValue();

            for (Object jsonObject : entryPoints ) {
                EntryPoint ep = modelFactory.createEntryPoint((JSONObject)jsonObject);
                plan.getEntryPoints().add(ep);
                ep.setPlan(plan);
            }
            return true;
        }
        return false;
    }
}
