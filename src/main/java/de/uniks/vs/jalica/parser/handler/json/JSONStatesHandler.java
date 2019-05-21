package de.uniks.vs.jalica.parser.handler.json;

import de.uniks.vs.jalica.parser.handler.json.JSONHandler;
import de.uniks.vs.jalica.unknown.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Node;

import java.util.HashMap;

public class JSONStatesHandler extends JSONHandler {

    public static final String STATES = "states";

    @Override
    public boolean handleIt(Object obj, Plan plan, ModelFactory modelFactory) {
        HashMap.Entry entry = (HashMap.Entry) obj;

        if (STATES.equals(entry.getKey())) {
            JSONArray states = (JSONArray) entry.getValue();

            for (Object object : states ) {
                JSONObject jsonObject = (JSONObject) object;

                if ("State".equals(jsonObject.get("type")))                {
                    State state = modelFactory.createState(jsonObject);
                    plan.getStates().add(state);
                    state.setInPlan(plan);
                }
                else if ("SuccessState".equals(jsonObject.get("type")))                {
                    SuccessState state = modelFactory.createSuccessState(jsonObject);
                    plan.getSuccessStates().add(state);
                    state.setInPlan(plan);
                }
                else if ("FailureState".equals(jsonObject.get("type")))                {
                    FailureState state = modelFactory.createFailureState(jsonObject);
                    plan.getFailureStates().add(state);
                    state.setInPlan(plan);
                }
//                else if ("alica:SuccessState".equals(typeString))
//                {
//                    SuccessState suc = modelFactory.createSuccessState(node);
//                    suc.setInPlan(plan);
//                    plan.getSuccessStates().add(suc);
//                    plan.getStates().add(suc);
//                }
//                else if ("alica:FailureState".equals(typeString))
//                {
//                    FailureState fail = modelFactory.createFailureState(node);
//                    fail.setInPlan(plan);
//                    plan.getFailureStates().add(fail);
//                    plan.getStates().add(fail);
//                }
                else {
                    modelFactory.getAE().abort("SH: Unknown State type:", jsonObject.get("type").toString());
                }
            }
            return true;

        }
        return false;
    }
}
