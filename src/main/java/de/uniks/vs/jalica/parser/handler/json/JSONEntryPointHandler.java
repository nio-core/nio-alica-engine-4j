package de.uniks.vs.jalica.parser.handler.json;

import de.uniks.vs.jalica.unknown.EntryPoint;
import de.uniks.vs.jalica.unknown.ModelFactory;
import de.uniks.vs.jalica.unknown.Plan;
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
                plan.getEntryPoints().put(ep.getID(), ep);
                ep.setPlan(plan);
            }
            return true;
        }
        return false;
    }
}
