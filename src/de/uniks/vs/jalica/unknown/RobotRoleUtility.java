package de.uniks.vs.jalica.unknown;

import java.util.Comparator;

/**
 * Created by alex on 14.07.17.
 */
public class RobotRoleUtility {
    private final double dutility;
    private final RobotProperties robProperties;
    private final Role role;
    private double utilityValue;

    public RobotRoleUtility(double dutility, RobotProperties robProperties, Role role) {

        this.dutility = dutility;
        this.robProperties = robProperties;
        this.role = role;
    }

    public Role getRole() {
        return role;
    }

    public RobotProperties getRobot() {
        return robProperties;
    }


    public int equals(RobotRoleUtility other) {

        if (other.getRole().getId() != this.getRole().getId())
            return other.getRole().getId() < this.getRole().getId()? 1:-1;

        if (other.getUtilityValue() != this.getUtilityValue())
            return other.getUtilityValue() < this.getUtilityValue()? 1:-1;

        if (other.getRobot().getId() != this.getRobot().getId())
            return other.getRobot().getId() < this.getRobot().getId()? 1:-1;

        return 0;
    }

    public static <T> Comparator<RobotRoleUtility> compareTo() {

        return new Comparator<RobotRoleUtility>() {

            @Override
            public int compare(RobotRoleUtility o1, RobotRoleUtility o2) {
                return o1.equals(o2);
            }

        };
    }

    public double getUtilityValue() {
        return utilityValue;
    }

//        if(otherOne->getRole()->getId() != thisOne->getRole()->getId())
//        return otherOne->getRole()->getId() < thisOne->getRole()->getId();
//
//        if(otherOne->getUtilityValue() != thisOne->getUtilityValue())
//            return otherOne->getUtilityValue() < thisOne->getUtilityValue();
//
//        if(otherOne->getRobot()->getId() != thisOne->getRobot()->getId())
//        return otherOne->getRobot()->getId() < thisOne->getRobot()->getId();
//
//        return false;

}
