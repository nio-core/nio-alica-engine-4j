package de.uniks.vs.jalica.engine.common;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.model.AlicaElement;

import java.util.ArrayList;

/**
 * Created by alex on 13.07.17.
 */
@Deprecated
public class Capability extends AlicaElement {
    ArrayList<CapValue> capValues = new ArrayList<>();

    public ArrayList<CapValue> getCapValues() {
        return capValues;
    }

    public double similarityValue(CapValue roleVal, CapValue agentVal) {
        int nCount = capValues.size();

        int rlIndex = -1;
        int rbIndex = -1;
        int index = 0;

        // determine the index of both given capability values
        for (CapValue cap : capValues)
        {
            if (cap == roleVal)
            {
                rlIndex = index;
            }
            if (cap == agentVal)
            {
                rbIndex = index;
            }
            ++index;
        }

        if (rlIndex == -1)
        {
            CommonUtils.aboutError("Capability::similarityValue: Role not found!");
        }
        if (rbIndex == -1)
        {
            CommonUtils.aboutError("Capability::similarityValue: Agent not found!");
//            throw Exception();
        }

        if (nCount == 1)
        {
            // we found both values and there is only one value, so both must be the same
            return 1;
        }

        // this won't work, in case of only one value (nCount=1), therefore extra handling above
        return (nCount - 1 - Math.abs(rlIndex - rbIndex)) / (nCount - 1);

    }
}
