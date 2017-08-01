package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.common.AssignmentCollection;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by alex on 17.07.17.
 */
public interface IAssignment {

    short getEntryPointCount();

    ArrayList<Integer> getRobotsWorkingAndFinished(EntryPoint ep);

    ArrayList<Integer> getUniqueRobotsWorkingAndFinished(EntryPoint ep);

    void setMin(double min);

    void setMax(double max);

    Vector<Integer> getUnassignedRobots();

    AssignmentCollection getEpRobotsMapping();
}
