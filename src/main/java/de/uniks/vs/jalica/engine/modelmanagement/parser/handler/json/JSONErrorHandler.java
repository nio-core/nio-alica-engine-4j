package de.uniks.vs.jalica.engine.modelmanagement.parser.handler.json;

import de.uniks.vs.jalica.engine.modelmanagement.ModelFactory;
import de.uniks.vs.jalica.engine.model.Plan;

import java.util.HashMap;

public class JSONErrorHandler extends JSONHandler {
    @Override
    public boolean handleIt(Object obj, Plan plan, ModelFactory modelFactory) {
        HashMap.Entry entry = (HashMap.Entry) obj;

        if ("name".equals(entry.getKey()) && plan.getName().equals(entry.getValue()))
            return true;

        if ("id".equals(entry.getKey()) && (plan.getID() == (long)entry.getValue()))
            return true;

        if("comment".equals(entry.getKey()))
            return true;

        if("relativeDirectory".equals(entry.getKey()))
            return true;

        return false;
    }
}
