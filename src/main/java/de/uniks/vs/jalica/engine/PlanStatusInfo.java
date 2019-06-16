package de.uniks.vs.jalica.engine;

public class PlanStatusInfo {

    public PlanStatus status;
    public PlanActivity active;
    public AlicaTime stateStartTime;
    public AlicaTime planStartTime;

    public int failCount;
    public boolean failHandlingNeeded;
    public boolean allocationNeeded;
    public /*mutable*/ EvalStatus runTimeConditionStatus;

    public PlanStatusInfo() {
        status = PlanStatus.Running;
        failCount= 0;
        stateStartTime = new AlicaTime();
        planStartTime = new AlicaTime();
        active = PlanActivity.InActive;
        allocationNeeded = false;
        failHandlingNeeded = false;
        runTimeConditionStatus = EvalStatus.Unknown;
    }

}
