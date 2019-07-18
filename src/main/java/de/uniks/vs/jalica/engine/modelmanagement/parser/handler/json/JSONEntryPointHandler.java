package de.uniks.vs.jalica.engine.modelmanagement.parser.handler.json;

import de.uniks.vs.jalica.engine.AgentStatePairs;
import de.uniks.vs.jalica.engine.model.EntryPoint;
import de.uniks.vs.jalica.engine.modelmanagement.ModelFactory;
import de.uniks.vs.jalica.engine.model.Plan;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class JSONEntryPointHandler extends JSONHandler {

    public static final String ENTRY_POINTS = "entryPoints";
    private ArrayList<EntryPoint> constructedEntryPoints;

    @Override
    public boolean handleIt(Object obj, Plan plan, ModelFactory modelFactory) {

        HashMap.Entry entry = (HashMap.Entry) obj;

        if (ENTRY_POINTS.equals(entry.getKey())) {
            constructedEntryPoints = new ArrayList<>();

            JSONArray entryPoints = (JSONArray) entry.getValue();

            for (Object jsonObject : entryPoints ) {
                EntryPoint ep = modelFactory.createEntryPoint((JSONObject)jsonObject);
                plan.getEntryPoints().add(ep);
                ep.setPlan(plan);

                constructedEntryPoints.add(ep);
            }
            constructedEntryPoints.sort((EntryPoint ep1, EntryPoint ep2) -> ep1.getID() < ep2.getID()? -1: 1);

            for (int i = 0; i < entryPoints.size(); i++) {
                constructedEntryPoints.get(i).setIndex(i);
            }
            return true;
        }
        return false;
    }
}
