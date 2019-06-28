package de.uniks.vs.jalica.engine.model;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by alex on 03.08.17.
 * Updated 23.6.19
 */
public class PostCondition extends Condition {

    public PostCondition() {
        super();
    }

    @Override
    public String toString() {
        String  ss = "";
        String indent = "";
        ss += indent + "#PostCondition: " + getName() + " " + getID() + "\n";
        ss += indent + "\t ConditionString: " + getConditionString() + "\n";
        ss += indent + "#PostCondition" + "\n";
        return ss;
    }
}
