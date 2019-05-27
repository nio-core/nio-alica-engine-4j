package de.uniks.vs.jalica.engine.planselection;

import de.uniks.vs.jalica.engine.Assignment;
import de.uniks.vs.jalica.engine.planselection.IAssignment;

/**
 * Created by alex on 21.07.17.
 */
public interface ITaskAssignment {

    Assignment getNextBestAssignment(IAssignment oldAss);
}
