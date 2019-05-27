package de.uniks.vs.jalica.engine.modelmanagement.parser.handler.xml;

import de.uniks.vs.jalica.engine.common.CommonUtils;
import de.uniks.vs.jalica.engine.modelmanagement.ModelFactory;
import de.uniks.vs.jalica.engine.model.Plan;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Created by alex on 18.09.17.
 */
public class AttributeHandler extends XMLHandler {

    @Override
    public boolean handleIt(Node node, Plan plan, ModelFactory modelFactory) {

        if (modelFactory.getRep().getPlans().containsKey(plan.getID()))
            return false;

        Element element = node.getOwnerDocument().getDocumentElement();

        String isMasterPlanAttr = element.getAttributes().getNamedItem("masterPlan").getTextContent();

        if (!isMasterPlanAttr.isEmpty())
        {
//            transform(isMasterPlanAttr.begin(), isMasterPlanAttr.end(), isMasterPlanAttr.begin(), ::tolower);

            isMasterPlanAttr = isMasterPlanAttr.toLowerCase();

            if ("true".equals(isMasterPlanAttr))
            {
                plan.setMasterPlan(true);
            }
        }

        String attr = element.getAttributes().getNamedItem("minCardinality").getTextContent();

        if (!attr.isEmpty())
        {
            plan.setMinCardinality(CommonUtils.stoi(attr));
        }
        attr = element.getAttributes().getNamedItem("maxCardinality").getTextContent();

        if (!attr.isEmpty())
        {
            plan.setMaxCardinality(CommonUtils.stoi(attr));
        }
        attr = element.getAttributes().getNamedItem("utilityThreshold").getTextContent();

        if (!attr.isEmpty())
        {
            plan.setUtilityThreshold(CommonUtils.stod(attr));
        }
        attr = element.getAttributes().getNamedItem("destinationPath").getTextContent();

        if (!attr.isEmpty())
        {
            plan.setDestinationPath(attr);
        }

        // insert into elements ma
        modelFactory.addElement(plan);
        // insert into plan repository map
        modelFactory.getRep().getPlans().put(plan.getID(), plan);

        return false;
    }
}
