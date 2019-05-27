package de.uniks.vs.jalica.engine.modelmanagement.parser.handler.xml;

import de.uniks.vs.jalica.engine.common.CommonUtils;
import de.uniks.vs.jalica.engine.modelmanagement.ModelFactory;
import de.uniks.vs.jalica.engine.model.Plan;
import org.w3c.dom.Node;

/**
 * Created by alex on 18.09.17.
 */
public abstract class XMLHandler {

    public boolean handle(Node node, Plan plan, ModelFactory modelFactory) {
        boolean result = handleIt(node, plan, modelFactory);

        if (result) {
            if (CommonUtils.XTH_DEBUG_debug) System.out.println("XTH: "+ this.getClass().getSimpleName() + " " + node.getNodeName());
        }
        return result;
    }
    public abstract boolean handleIt(Node node, Plan plan, ModelFactory modelFactory);
}
