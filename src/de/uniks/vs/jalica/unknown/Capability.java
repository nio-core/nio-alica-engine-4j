package de.uniks.vs.jalica.unknown;

import java.util.ArrayList;

/**
 * Created by alex on 13.07.17.
 */
public class Capability extends AlicaElement {
    ArrayList<CapValue> capValues = new ArrayList<>();

    public ArrayList<CapValue> getCapValues() {
        return capValues;
    }

    public double similarityValue(CapValue roleVal, CapValue robotVal) {
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
            if (cap == robotVal)
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
            CommonUtils.aboutError("Capability::similarityValue: Robot not found!");
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
