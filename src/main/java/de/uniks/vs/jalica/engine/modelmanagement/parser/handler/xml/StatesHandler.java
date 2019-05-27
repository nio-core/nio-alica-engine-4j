package de.uniks.vs.jalica.engine.modelmanagement.parser.handler.xml;

import de.uniks.vs.jalica.engine.model.FailureState;
import de.uniks.vs.jalica.engine.model.Plan;
import de.uniks.vs.jalica.engine.model.State;
import de.uniks.vs.jalica.engine.model.SuccessState;
import de.uniks.vs.jalica.engine.modelmanagement.ModelFactory;
import org.w3c.dom.Node;

/**
 * Created by alex on 18.09.17.
 */
public class StatesHandler extends XMLHandler {

    public static final String STATES = "states";

    @Override
    public boolean handleIt(Node node, Plan plan, ModelFactory modelFactory) {

        String val = node.getNodeName();

        if (STATES.equals(val))
        {
//            node = node.getFirstChild().getNextSibling();

            String name = "";
//				String typePtr = curChild.getAttributes().getNamedItem("xsi:type").getTextContent();
            Node namedItem = node.getAttributes().getNamedItem("xsi:type");
            String typePtr = "";

            if (namedItem != null)
                typePtr = namedItem.getTextContent(); // into plan search for this item
            String typeString = "";
            //Normal State
            if (typePtr != null)
            {
                typeString = typePtr;
            }

            if (typeString.isEmpty())
            {
                State state = modelFactory.createState(node);
                plan.getStates().add(state);
                state.setInPlan(plan);
            }
            else if ("alica:SuccessState".equals(typeString))
            {
                SuccessState suc = modelFactory.createSuccessState(node);
                suc.setInPlan(plan);
                plan.getSuccessStates().add(suc);
                plan.getStates().add(suc);
            }
            else if ("alica:FailureState".equals(typeString))
            {
                FailureState fail = modelFactory.createFailureState(node);
                fail.setInPlan(plan);
                plan.getFailureStates().add(fail);
                plan.getStates().add(fail);
            }
            else
            {
                modelFactory.getAE().abort("SH: Unknown State type:", typePtr);
            }
            return true;
        }

        return false;
    }
}
