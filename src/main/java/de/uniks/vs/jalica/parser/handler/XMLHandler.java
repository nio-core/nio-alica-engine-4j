package de.uniks.vs.jalica.parser.handler;

import de.uniks.vs.jalica.unknown.ModelFactory;
import de.uniks.vs.jalica.unknown.Plan;
import org.w3c.dom.Node;

/**
 * Created by alex on 18.09.17.
 */
public abstract class XMLHandler {

    public boolean handle(Node node, Plan plan, ModelFactory modelFactory) {
        boolean result = handleIt(node, plan, modelFactory);

        if (result) {
            modelFactory.getAE().print("XTH: "+ this.getClass().getSimpleName() + " " + node.getNodeName());
        }
        return result;
    }
    public abstract boolean handleIt(Node node, Plan plan, ModelFactory modelFactory);
}
