package de.uniks.vs.jalica.unknown.parser;

import de.uniks.vs.jalica.unknown.*;
import org.w3c.dom.Node;

/**
 * Created by alex on 18.09.17.
 */
public class ConditionsHandler extends XMLHandler {

    public static final String CONDITIONS = "conditions";

    @Override
    public boolean handleIt(Node node, Plan plan, ModelFactory modelFactory) {

        String val = node.getNodeName();

        if (CONDITIONS.equals(val))
        {
            String typePtr = node.getAttributes().getNamedItem("xsi:type").getTextContent();
            String typeString = "";
            if (typePtr != null)
            {
                typeString = typePtr;
            }
            if (typeString.isEmpty())
            {
                modelFactory.getAE().abort("MF: Condition without xsi:type in plan", plan.getName());
            }
            else if ("alica:RuntimeCondition".equals(typeString))
            {
                RuntimeCondition rc = modelFactory.createRuntimeCondition(node);
                rc.setAbstractPlan(plan);
                plan.setRuntimeCondition(rc);
            }
            else if ("alica:PreCondition".equals(typeString))
            {
                PreCondition p = modelFactory.createPreCondition(node);
                p.setAbstractPlan(plan);
                plan.setPreCondition(p);
            }
            else if ("alica:PostCondition".equals(typeString))
            {
                PostCondition p = modelFactory.createPostCondition(node);
                plan.setPostCondition(p);
            }
            else
            {
                modelFactory.getAE().abort("MF: Unknown Condition type", node.toString());
            }
        }
        return false;
    }


}
