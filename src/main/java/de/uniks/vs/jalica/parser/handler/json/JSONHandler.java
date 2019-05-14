package de.uniks.vs.jalica.parser.handler.json;

import de.uniks.vs.jalica.unknown.CommonUtils;
import de.uniks.vs.jalica.unknown.ModelFactory;
import de.uniks.vs.jalica.unknown.Plan;

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
