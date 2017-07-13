package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.behaviours.BehaviourCreator;
import de.uniks.vs.jalica.behaviours.BehaviourPool;
import de.uniks.vs.jalica.common.AssignmentCollection;
import de.uniks.vs.jalica.common.Logger;
import de.uniks.vs.jalica.common.UtilityFunction;
import de.uniks.vs.jalica.conditions.ConditionCreator;
import de.uniks.vs.jalica.constraints.ConstraintCreator;
import de.uniks.vs.jalica.dummy_proxy.AlicaDummyCommunication;
import de.uniks.vs.jalica.dummy_proxy.AlicaSystemClock;
import de.uniks.vs.jalica.parser.PlanParser;
import de.uniks.vs.jalica.teamobserver.PlanRepository;
import de.uniks.vs.jalica.teamobserver.TeamObserver;
import de.uniks.vs.jalica.reasoner.CGSolver;
import de.uniks.vs.jalica.supplementary.SystemConfig;
import de.uniks.vs.jalica.unknown.*;
import de.uniks.vs.jalica.utilfunctions.UtilityFunctionCreator;

/**
 * Created by alex on 13.07.17.
 */
public class AlicaEngine {
    private AlicaSystemClock clock;
    private AlicaDummyCommunication communicator;
    private SystemConfig sc = SystemConfig.getInstance();
    private Boolean maySendMessages;
    private Boolean useStaticRoles;
    private boolean terminating;
    private boolean stepEngine;
    private PlanRepository planRepository;
    private PlanParser planParser;
    private Plan masterPlan;
    private TeamObserver teamObserver;
    private BehaviourPool behaviourPool;
    private RoleSet roleSet;
    private RoleAssignment roleAssignment;
    private SyncModul syncModul;
    private ExpressionHandler expressionHandler;
    private boolean stepCalled;
    private PartialAssignmentPool pap;
    private PlanBase planBase;
    private PlanSelector planSelector;
    private AuthorityManager auth;
    private Logger log;
    private VariableSyncModule variableSyncModule;

    public void setIAlicaClock(AlicaSystemClock clock) {
        this.clock = clock;
    }

    public void setCommunicator(AlicaDummyCommunication communicator) {
        this.communicator = communicator;
    }

    public void addSolver(String gradientsolver, CGSolver cgSolver) {

    }

    public boolean init(BehaviourCreator bc, ConditionCreator cc, UtilityFunctionCreator uc, ConstraintCreator crc,
                                                String roleSetName, String masterPlanName, String roleSetDir, boolean stepEngine) {

        this.maySendMessages =  Boolean.valueOf(sc.get("Alica").get("Alica.SilentStart"));
        this.useStaticRoles = Boolean.valueOf(sc.get("Alica").get("Alica.UseStaticRoles"));
        AssignmentCollection.maxEpsCount = Short.valueOf(sc.get("Alica").get("Alica.MaxEpsPerPlan"));
        AssignmentCollection.allowIdling  = Boolean.valueOf(sc.get("Alica").get("Alica.AllowIdling"));

        this.terminating = false;
        this.stepEngine = stepEngine;

        if (this.planRepository == null)
        {
            this.planRepository = new PlanRepository();
        }
        if (this.planParser == null)
        {
            this.planParser = new PlanParser(this, this.planRepository);
        }
        if (this.masterPlan == null)
        {
            this.masterPlan = this.planParser.parsePlanTree(masterPlanName);
        }
        if (this.roleSet == null)
        {
            this.roleSet = this.planParser.parseRoleSet(roleSetName, roleSetDir);
        }
        if (this.behaviourPool == null)
        {
            this.behaviourPool = new BehaviourPool(this);
        }
        if (this.teamObserver == null)
        {
            this.teamObserver = new TeamObserver(this);
        }
        if (this.roleAssignment == null)
        {
            if (this.useStaticRoles)
            {
                this.roleAssignment = new StaticRoleAssignment(this);
            }
			else
            {
                this.roleAssignment = new RoleAssignment(this);
            }
            // the communicator is expected to be set before init() is called
            this.roleAssignment.setCommunication(communicator);
        }
        if (this.syncModul == null)
        {
            this.syncModul = new SyncModul(this);
        }
        if (this.expressionHandler == null)
        {
            this.expressionHandler = new ExpressionHandler(this, cc, uc, crc);
        }

        this.stepCalled = false;
        boolean everythingWorked = true;
        everythingWorked = this.behaviourPool.init(bc);
        this.auth = new AuthorityManager(this);
        this.log = new Logger(this);
        this.teamObserver.init();
        this.roleAssignment.init();

        if (this.pap == null)
        {
            pap = new PartialAssignmentPool();
        }
        if (planSelector == null)
        {
            this.planSelector = new PlanSelector(this, pap);
        }

        this.auth.init();
        this.planBase = new PlanBase(this, this.masterPlan);
        this.expressionHandler.attachAll();
        UtilityFunction.initDataStructures(this);
        this.syncModul.init();

        if (this.variableSyncModule == null)
        {
            this.variableSyncModule = new VariableSyncModule(this);
        }
        if (this.getCommunicator() != null)
        {
            this.getCommunicator().startCommunication();
        }
        if (this.variableSyncModule != null)
        {
            this.variableSyncModule.init();
        }

        return everythingWorked;
    }

    private IAlicaCommunication getCommunicator() {
        return null;
    }

    public void start() {

    }
}
