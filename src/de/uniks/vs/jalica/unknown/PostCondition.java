package de.uniks.vs.jalica.unknown;

import java.util.ArrayList;
import java.util.Stack;
import java.util.Vector;

/**
 * Created by alex on 03.08.17.
 */
public class PostCondition extends Condition {

    PostCondition() {
        variables = new Vector<>();
        quantifiers = new ArrayList<>();
    }

    PostCondition(long id) {
        super();
        this.id = id;
    }

    @Override
    public String toString() {
        return  "#PostCondition: " + this.name + " " + this.id  +"\n\t ConditionString: " + this.conditionString + "\n#PostCondition\n";
    }
}
