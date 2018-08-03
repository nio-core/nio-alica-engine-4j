package de.uniks.vs.jalica.parser.handler;

import de.uniks.vs.jalica.unknown.ModelFactory;
import de.uniks.vs.jalica.unknown.Plan;
import de.uniks.vs.jalica.unknown.Transition;
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
