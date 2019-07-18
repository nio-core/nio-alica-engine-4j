package de.uniks.vs.jalica.engine.teammanagement.view.iterator;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.idmanagement.ID;
import de.uniks.vs.jalica.engine.teammanagement.Agent;

import java.util.Iterator;
import java.util.LinkedHashMap;

//TODO: implmentation is missing
public class ActiveAgentIdIterator extends ActiveAgentBaseIterator{

    public ActiveAgentIdIterator(Iterator it, LinkedHashMap<ID, Agent> map) {
        super(it, map);
        CommonUtils.aboutNoImpl();
    }
}
