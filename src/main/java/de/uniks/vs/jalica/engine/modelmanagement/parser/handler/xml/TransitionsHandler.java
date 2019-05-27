package de.uniks.vs.jalica.engine.modelmanagement.parser.handler.xml;

import de.uniks.vs.jalica.engine.modelmanagement.ModelFactory;
import de.uniks.vs.jalica.engine.model.Plan;
import de.uniks.vs.jalica.engine.model.Transition;
import org.w3c.dom.Node;

/**
 * Created by alex on 18.09.17.
 */
public class TransitionsHandler extends XMLHandler {

    public static final String TRANSITIONS = "transitions";

    @Override
    public boolean handleIt(Node node, Plan plan, ModelFactory modelFactory) {

        String val = node.getNodeName();

        if (TRANSITIONS.equals(val))
        {
            Transition tran = modelFactory.createTransition(node, plan);
            plan.getTransitions().add(tran);
            return true;

        }
        return false;
    }


}
