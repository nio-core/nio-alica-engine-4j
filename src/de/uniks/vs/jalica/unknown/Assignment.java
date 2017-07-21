package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.behaviours.BehaviourConfiguration;
import de.uniks.vs.jalica.common.AssignmentCollection;

import java.util.ArrayList;
import java.util.Set;
import java.util.Vector;

/**
 * Created by alex on 17.07.17.
 */
public class Assignment implements IAssignment{

    private StateCollection robotStateMapping;
    private SuccessCollection epSuccessMapping;
    private AssignmentCollection epRobotsMapping;
    private BehaviourConfiguration plan;

    public void moveRobots(State from, State to) {

        Set<Integer> movingRobots = this.robotStateMapping.getRobotsInState(from);
        if (to == null)
        {
            System.out.println("Ass: MoveRobots is given a State which is NULL!");
        }
        for (int r : movingRobots)
        {
            this.robotStateMapping.setState(r, to);
        }
    }

    public SuccessCollection getEpSuccessMapping() {
        return epSuccessMapping;
    }

    public boolean removeRobot(int robotId) {

        this.robotStateMapping.removeRobot(robotId);
        Vector<Integer> curRobots;
        for (int i = 0; i < this.epRobotsMapping.getSize(); i++)
        {
            curRobots = this.epRobotsMapping.getRobots(i);
            Integer iter = CommonUtils.find(curRobots, 0, curRobots.size() - 1, robotId);
            if (iter != curRobots.size()-1)
            {
                curRobots.remove(iter);
                return true;
            }
        }
        return false;
    }

    public void addRobot(int id, EntryPoint e, State s) {
        if (e == null)
        {
            return;
        }
        this.robotStateMapping.setState(id, s);
        this.epRobotsMapping.getRobotsByEp(e).add(id);
        return;
    }

    public void clear() {
        this.robotStateMapping.clear();
        this.epRobotsMapping.clear();
        this.epSuccessMapping.clear();
    }

    public void setAllToInitialState(ArrayList<Integer> robots, EntryPoint defep) {
        Vector<Integer> rlist = this.epRobotsMapping.getRobotsByEp(defep);
        for (int r : robots)
        {
            rlist.add(r);
        }
        for (int r : robots)
        {
            this.robotStateMapping.setState(r, defep.getState());
        }
    }

    public StateCollection getRobotStateMapping() {
        return robotStateMapping;
    }

    public BehaviourConfiguration getPlan() {
        return plan;
    }

    public EntryPoint getEntryPointOfRobot(int robot) {

        for (int i = 0; i < this.epRobotsMapping.getSize(); i++)
        {
            Integer iter = CommonUtils.find(this.epRobotsMapping.getRobots(i), 0, this.epRobotsMapping.getRobots(i).size() - 1,
                    Integer.valueOf(robot));
            if (iter != this.epRobotsMapping.getRobots(i).get(this.epRobotsMapping.getRobots(i).size()-1))
            {
                return this.epRobotsMapping.getEp(i);
            }
        }
        return null;
    }

    public Vector<Integer> getRobotsWorking(EntryPoint ep) {
        return this.getEpRobotsMapping().getRobotsByEp(ep);
    }

    public AssignmentCollection getEpRobotsMapping() {
        return epRobotsMapping;
    }
}
