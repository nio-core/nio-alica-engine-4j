package de.uniks.vs.jalica.unknown.parser;

import de.uniks.vs.jalica.unknown.ModelFactory;
import de.uniks.vs.jalica.unknown.Plan;
import de.uniks.vs.jalica.unknown.SyncTransition;
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
            SyncTransition st = modelFactory.createSyncTransition(node);
            st.setPlan(plan);
            plan.getSyncTransitions().add(st);
            return true;
        }
        return false;
    }
}
