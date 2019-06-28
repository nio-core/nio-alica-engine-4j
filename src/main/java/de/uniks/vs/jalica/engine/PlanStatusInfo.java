package de.uniks.vs.jalica.engine;

public class PlanStatusInfo {

    public PlanStatus.Status status;
    public PlanActivity.Activity active;
    public AlicaTime stateStartTime;
    public AlicaTime planStartTime;

    public int failCount;
    public boolean failHandlingNeeded;
    public boolean allocationNeeded;
    public /*mutable*/ EvalStatus runTimeConditionStatus;

    public PlanStatusInfo() {
        status = PlanStatus.Status.Running;
        failCount= 0;
        stateStartTime = new AlicaTime();
        planStartTime = new AlicaTime();
        active = PlanActivity.Activity.InActive;
        allocationNeeded = false;
        failHandlingNeeded = false;
        runTimeConditionStatus = EvalStatus.Unknown;
    }

}
