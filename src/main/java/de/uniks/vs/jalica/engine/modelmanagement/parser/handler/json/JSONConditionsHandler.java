package de.uniks.vs.jalica.engine.modelmanagement.parser.handler.json;

import de.uniks.vs.jalica.engine.model.Plan;
import de.uniks.vs.jalica.engine.model.PostCondition;
import de.uniks.vs.jalica.engine.model.PreCondition;
import de.uniks.vs.jalica.engine.model.RuntimeCondition;
import de.uniks.vs.jalica.engine.modelmanagement.ModelFactory;
import org.json.simple.JSONObject;

import java.util.HashMap;

public class JSONConditionsHandler extends JSONHandler {

    public static final String CONDITIONS = "condition";
    public static final String RUNTIME_CONDITIONS = "runtimeCondition";
    public static final String PRE_CONDITIONS = "preCondition";
    public static final String POST_CONDITIONS = "postCondition";

    @Override
    public boolean handleIt(Object obj, Plan plan, ModelFactory modelFactory) {
        HashMap.Entry entry = (HashMap.Entry) obj;
        if (entry.getValue() == null)
            return true;

        else if (CONDITIONS.equals(entry.getKey())) {
//            String typePtr = node.getAttributes().getNamedItem("xsi:type").getTextContent();
//            String typeString = "";
//            if (typePtr != null)
//            {
//                typeString = typePtr;
//            }
//            if (typeString.isEmpty())
//            {
//                modelFactory.getAE().abort("MF: Condition without xsi:type in plan", plan.getName());
        } else if (RUNTIME_CONDITIONS.equals(entry.getKey())) {
            RuntimeCondition rc = modelFactory.createRuntimeCondition((JSONObject) entry.getValue());
            rc.setAbstractPlan(plan);
            plan.setRuntimeCondition(rc);
        } else if (PRE_CONDITIONS.equals(entry.getKey())) {
                PreCondition p = modelFactory.createPreCondition((JSONObject) entry.getValue());
                p.setAbstractPlan(plan);
                plan.setPreCondition(p);
//        } else if (POST_CONDITIONS.equals(entry.getKey())) {
//                PostCondition p = modelFactory.createPostCondition((JSONObject) entry.getValue());
//                plan.setPostCondition(p);
        } else {
//                modelFactory.getAE().abort("MF: Unknown Condition type", node.toString());
            return false;
        }
        return true;
    }
}
