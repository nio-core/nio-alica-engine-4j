package de.uniks.vs.jalica.engine.modelmanagement.parser.handler.xml;

import de.uniks.vs.jalica.engine.model.Plan;
import de.uniks.vs.jalica.engine.model.PostCondition;
import de.uniks.vs.jalica.engine.model.PreCondition;
import de.uniks.vs.jalica.engine.model.RuntimeCondition;
import de.uniks.vs.jalica.engine.modelmanagement.ModelFactory;
import org.w3c.dom.Node;

/**
 * Created by alex on 18.09.17.
 * Updated 23.6.19
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
                System.out.println("MF: Condition without xsi:type in plan "+ plan.getName());
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
//            else if ("alica:PostCondition".equals(typeString))
//            {
//                PostCondition p = modelFactory.createPostCondition(node);
//                plan.setPostCondition(p);
//            }
            else
            {
                System.out.println("MF: Unknown Condition type "+ node.toString());
            }
            return true;
        }
        return false;
    }


}
