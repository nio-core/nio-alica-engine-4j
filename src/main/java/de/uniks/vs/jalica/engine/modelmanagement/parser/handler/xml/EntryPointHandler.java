package de.uniks.vs.jalica.engine.modelmanagement.parser.handler.xml;

import de.uniks.vs.jalica.engine.model.EntryPoint;
import de.uniks.vs.jalica.engine.modelmanagement.ModelFactory;
import de.uniks.vs.jalica.engine.model.Plan;
import org.w3c.dom.Node;

/**
 * Created by alex on 18.09.17.
 * Updated 23.6.19
 */
public class EntryPointHandler extends XMLHandler {

    public static final String ENTRY_POINTS = "entryPoints";

    @Override
    public boolean handleIt(Node node, Plan plan, ModelFactory modelFactory) {

        String val = node.getNodeName();

        if (ENTRY_POINTS.equals(val))
        {
            EntryPoint ep = modelFactory.createEntryPoint(node);
            plan.getEntryPoints().add(ep);
            ep.setPlan(plan);
            return true;
        }
        return false;
    }


}
