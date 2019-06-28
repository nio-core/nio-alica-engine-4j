package de.uniks.vs.jalica.engine.modelmanagement.parser.handler.json;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.modelmanagement.ModelFactory;
import de.uniks.vs.jalica.engine.model.Plan;
import org.json.simple.JSONArray;

import java.util.HashMap;
import java.util.Map;

public class JSONAttributeHandler extends JSONHandler {

    @Override
    public boolean handleIt(Object obj, Plan plan, ModelFactory modelFactory) {
//        if (modelFactory.getPlanRepository().getPlans().containsKey(plan.extractID()))
//            return false;
        Map.Entry entry = (HashMap.Entry) obj;

        if (entry.getValue() instanceof JSONArray)
            return false;
//        JSONObject jsonObject = (JSONObject) ((HashMap.Entry) obj).getValue();

        if ("masterPlan".equals(entry.getKey())) {

            if ("true".equals(entry.getValue().toString().toLowerCase())) {
                plan.setMasterPlan(true);
            }
        }
        else if ("minCardinality".equals(entry.getKey())) {
            plan.setMinCardinality(CommonUtils.stoi(entry.getValue().toString()));
        }
        else if ("maxCardinality".equals(entry.getKey())) {
            plan.setMaxCardinality(CommonUtils.stoi(entry.getValue().toString()));
        }
        else if ("utilityThreshold".equals(entry.getKey())) {
            plan.setUtilityThreshold(CommonUtils.stod(entry.getValue().toString()));
        }
//        else if ("destinationPath".equals(entry.getKey())) {
//            plan.setDestinationPath(entry.getValue().toString());
//        }
        else {
            return false;
        }

        if (modelFactory.getPlanRepository().getPlans().containsKey(plan.getID())) {
            modelFactory.addElement(plan);
            modelFactory.getPlanRepository().getPlans().put(plan.getID(), plan);
        }
        return true;
    }
}
