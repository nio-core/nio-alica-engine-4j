package de.uniks.vs.jalica.engine.modelmanagement.parser.handler.xml;

import de.uniks.vs.jalica.engine.model.Synchronisation;
import de.uniks.vs.jalica.engine.modelmanagement.ModelFactory;
import de.uniks.vs.jalica.engine.model.Plan;
import de.uniks.vs.jalica.engine.common.SyncTransition;
import org.w3c.dom.Node;

/**
 * Created by alex on 18.09.17.
 */
public class SynchonisationsHandler extends XMLHandler {

    public static final String SYNCHRONISATIONS = "synchronisations";

    @Override
    public boolean handleIt(Node node, Plan plan, ModelFactory modelFactory) {

        String val = node.getNodeName();

        if (SYNCHRONISATIONS.equals(val))
        {
            Synchronisation st = modelFactory.createSynchronisation(node);
            st.setPlan(plan);
            plan.getSynchronisations().add(st);
            return true;
        }
        return false;
    }
}
