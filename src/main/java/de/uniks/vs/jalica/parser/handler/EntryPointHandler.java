package de.uniks.vs.jalica.parser.handler;

import de.uniks.vs.jalica.unknown.EntryPoint;
import de.uniks.vs.jalica.unknown.ModelFactory;
import de.uniks.vs.jalica.unknown.Plan;
import org.w3c.dom.Node;

/**
 * Created by alex on 18.09.17.
 */
public class EntryPointHandler extends XMLHandler {

    public static final String ENTRY_POINTS = "entryPoints";

    @Override
    public boolean handleIt(Node node, Plan plan, ModelFactory modelFactory) {

        String val = node.getNodeName();

        if (ENTRY_POINTS.equals(val))
        {
            EntryPoint ep = modelFactory.createEntryPoint(node);
            plan.getEntryPoints().put(ep.getID(), ep);
            ep.setPlan(plan);
            return true;
        }
        return false;
    }


}
