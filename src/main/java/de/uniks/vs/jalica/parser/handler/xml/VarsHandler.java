package de.uniks.vs.jalica.parser.handler.xml;

import de.uniks.vs.jalica.unknown.ModelFactory;
import de.uniks.vs.jalica.unknown.Plan;
import de.uniks.vs.jalica.unknown.Variable;
import org.w3c.dom.Node;

/**
 * Created by alex on 18.09.17.
 */
public class VarsHandler extends XMLHandler {

    public static final String VARS = "vars";

    @Override
    public boolean handleIt(Node node, Plan plan, ModelFactory modelFactory) {

        String val = node.getNodeName();

        if (VARS.equals(val))
        {
            Variable var = modelFactory.createVariable(node);
            plan.getVariables().add(var);

            return true;
        }
        return false;
    }
}
