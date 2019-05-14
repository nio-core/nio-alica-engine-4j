package de.uniks.vs.jalica.parser.handler.xml;

import de.uniks.vs.jalica.unknown.ModelFactory;
import de.uniks.vs.jalica.unknown.Plan;
import org.w3c.dom.Node;

/**
 * Created by alex on 18.09.17.
 */
public class ErrorHandler extends XMLHandler {

    @Override
    public boolean handleIt(Node node, Plan plan, ModelFactory modelFactory) {

        String val = node.getNodeName();
        modelFactory.getAE().abort("EH: Unhandled Plan Child: ", val);
        return true;
    }
}
