package de.uniks.vs.jalica.engine.modelmanagement.parser.handler.json;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.modelmanagement.ModelFactory;
import de.uniks.vs.jalica.engine.model.Plan;

public abstract class JSONHandler {

    public boolean handle(Object obj, Plan plan, ModelFactory modelFactory) {
        boolean result = handleIt(obj, plan, modelFactory);

        if (result) {
            if (CommonUtils.XTH_DEBUG_debug) System.out.println("JTH: "+ this.getClass().getSimpleName() + " " + obj.toString());
        }
        return result;
    }
    public abstract boolean handleIt(Object obj, Plan plan, ModelFactory modelFactory);
}
