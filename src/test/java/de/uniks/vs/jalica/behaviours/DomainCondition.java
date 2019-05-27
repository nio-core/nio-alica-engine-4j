package de.uniks.vs.jalica.behaviours;

import de.uniks.vs.jalica.engine.BasicCondition;
import de.uniks.vs.jalica.engine.RunningPlan;

public class DomainCondition extends BasicCondition {

    public DomainCondition() {
        super();
//        this.wm = ttb::TTBWorldModel::get();
    }

    public boolean checkLastCommand(/*robot_control::RobotCommand::_cmd_type cmd*/)
    {
//        if ((wm->rawSensorData.getOwnRobotOnOff() != nullptr) && wm->rawSensorData.getOwnRobotOnOff()->cmd == cmd)
//        {
//            return true;
//        }
        return false;
    }

    public boolean fullyCharged()
    {
//        auto core = wm->rawSensorData.getOwnMobileBaseSensorState();
//        if (core->charger == kobuki_msgs::SensorState::DOCKING_CHARGED
//                || core->charger == kobuki_msgs::SensorState::ADAPTER_CHARGED)
//        {
//            return true;
//        }
        return false;
    }

    public boolean isCharging()
    {
//        auto core = wm->rawSensorData.getOwnMobileBaseSensorState();
//        if (core->charger == kobuki_msgs::SensorState::DOCKING_CHARGING
//                || core->charger == kobuki_msgs::SensorState::ADAPTER_CHARGING)
//        {
//            return true;
//        }
        return false;
    }

    @Override
    public boolean evaluate(RunningPlan rp) {
        return false;
    }
}
