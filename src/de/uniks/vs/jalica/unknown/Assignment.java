package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.behaviours.BehaviourConfiguration;
import de.uniks.vs.jalica.common.AssignmentCollection;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Set;
import java.util.Vector;

/**
 * Created by alex on 17.07.17.
 */
public class Assignment implements IAssignment{

    private  int max;
    private  int min;
    private StateCollection robotStateMapping;
    private AssignmentCollection epRobotsMapping;
    private Plan plan;
    private SuccessCollection epSucMapping;
    private Vector<Integer> unassignedRobots;

    public Assignment(Plan p, AllocationAuthorityInfo aai) {
        this.plan = p;
        this.max = 1;
        this.min = 1;

        this.epRobotsMapping = new AssignmentCollection(p.getEntryPoints().size());

        Vector<Integer> curRobots;
        short i = 0;
        for ( Long epPair : p.getEntryPoints().keySet()) {
            // set the entrypoint
            if (!this.epRobotsMapping.setEp(i, p.getEntryPoints().get(epPair))) {
                System.err.println("Ass: AssignmentCollection Index out of entrypoints bounds!");
                try {
                    throw new Exception();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            curRobots = new Vector<Integer>();
            for (EntryPointRobots epRobots : aai.entryPointRobots) {
                // find the right entrypoint
                if (epRobots.entrypoint == p.getEntryPoints().get(epPair).getId()) {
                    // copy robots
                    for (int robot : epRobots.robots) {
                        curRobots.add(robot);
                    }

                    // set the robots
                    if (!this.epRobotsMapping.setRobots(i, curRobots)) {
                        System.err.println("Ass: AssignmentCollection Index out of robots bounds!");
                        try {
                            throw new Exception();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    break;
                }
            }
        }
    }

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
        return epSucMapping;
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
        this.epSucMapping.clear();
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

    public Plan getPlan() {
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

    public boolean updateRobot(int robot, EntryPoint ep) {
        boolean ret = false;
        for (int i = 0; i < this.epRobotsMapping.getSize(); i++)
        {
            if (this.epRobotsMapping.getEp(i) == ep)
            {
                if (CommonUtils.find(this.epRobotsMapping.getRobots(i),0, this.epRobotsMapping.getRobots(i).size()-1,
                    robot) != this.epRobotsMapping.getRobots(i).lastElement())
                {
                    return false;
                }
				else
                {
                    this.epRobotsMapping.getRobots(i).add(robot);
                    ret = true;
                }
            }
			else
            {
                Integer iter = CommonUtils.find(this.epRobotsMapping.getRobots(i), 0, this.epRobotsMapping.getRobots(i).size() - 1, robot);
                if (iter != this.epRobotsMapping.getRobots(i).lastElement())
                {
                    this.epRobotsMapping.getRobots(i).remove(iter);
                    ret = true;
                }
            }
        }
        if (ret)
        {
            this.robotStateMapping.setState(robot, ep.getState());
        }
        return ret;
    }

    public Vector<Integer> getAllRobots() {
        Vector<Integer> ret = new Vector<Integer>();
        for (int i = 0; i < this.epRobotsMapping.getSize(); i++)
        {
            for (int j = 0; j < this.epRobotsMapping.getRobots(i).size(); j++)
            {
                ret.add(this.epRobotsMapping.getRobots(i).get(j));
            }
        }
        return ret;
    }

    public boolean isValid() {
        Vector<ArrayList<Integer>> success = this.epSucMapping.getRobots();

        for (int i = 0; i < this.epRobotsMapping.getSize(); ++i)
        {
            int c = this.epRobotsMapping.getRobots(i).size() + success.get(i).size();
            if (c > this.epRobotsMapping.getEp(i).getMaxCardinality()
                || c < this.epRobotsMapping.getEp(i).getMinCardinality())
            {
                return false;
            }
        }
        return true;
    }

    public Vector<Integer> getUnassignedRobots() {
        return unassignedRobots;
    }


    public int getMax() {
        return max;
    }

    public boolean isSuccessfull() {
        for (int i = 0; i < this.epSucMapping.getCount(); i++)
        {
            if (this.epSucMapping.getEntryPoints().get(i).getSuccessRequired())
            {
                if (!(this.epSucMapping.getRobots().get(i).size() > 0
                    && this.epSucMapping.getRobots().get(i).size()
                    >= this.epSucMapping.getEntryPoints().get(i).getMinCardinality()))
                {
                    return false;
                }
            }

        }
        return true;
    }

    @Override
    public short getEntryPointCount() {
        return 0;
    }

    @Override
    public ArrayList<Integer> getRobotsWorkingAndFinished(EntryPoint ep) {
        return null;
    }

    @Override
    public ArrayList<Integer> getUniqueRobotsWorkingAndFinished(EntryPoint ep) {
        ArrayList<Integer>  ret = new ArrayList<>();
        //if (this.plan.getEntryPoints().find(ep.getId()) != this.plan.getEntryPoints().end())
        {
            Vector<Integer> robots = this.epRobotsMapping.getRobotsByEp(ep);

            for (int i = 0; i < robots.size(); i++)
            {
                ret.add(robots.get(i));
            }
            for (Integer r : this.epSucMapping.getRobots(ep))
            {
                if (CommonUtils.find(ret, 0, ret.size()-1, r) == ret.get(ret.size()-1))
                {
                    ret.add(r);
                }
            }
        }
        return ret;
    }

    @Override
    public void setMin(double min) {

    }

    @Override
    public void setMax(double max) {

    }
}
