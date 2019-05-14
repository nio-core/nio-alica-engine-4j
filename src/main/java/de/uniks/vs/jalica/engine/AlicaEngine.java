package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.behaviours.BehaviourPool;
import de.uniks.vs.jalica.behaviours.IBehaviourCreator;
import de.uniks.vs.jalica.common.AssignmentCollection;
import de.uniks.vs.jalica.common.Logger;
import de.uniks.vs.jalica.common.UtilityFunction;
import de.uniks.vs.jalica.behaviours.ConditionCreator;
import de.uniks.vs.jalica.behaviours.ConstraintCreator;
import de.uniks.vs.jalica.parser.PlanParser;
import de.uniks.vs.jalica.reasoner.Solver;
import de.uniks.vs.jalica.teamobserver.PlanRepository;
import de.uniks.vs.jalica.teamobserver.TeamObserver;
import de.uniks.vs.jalica.reasoner.CGSolver;
import de.uniks.vs.jalica.supplementary.SystemConfig;
import de.uniks.vs.jalica.unknown.*;
import de.uniks.vs.jalica.behaviours.UtilityFunctionCreator;

import java.util.HashMap;

/**
 * Created by alex on 13.07.17.
 */
public class AlicaEngine {

    private SystemConfig sc;

    private boolean maySendMessages;
    private boolean useStaticRoles;
    private boolean terminating;
    private boolean stepEngine;
    private boolean stepCalled;

    private AlicaCommunication communicator;
    private PlanRepository planRepository;
    private PlanParser planParser;
    private Plan masterPlan;
    private TeamObserver teamObserver;
    private IBehaviourPool behaviourPool;
    private RoleSet roleSet;
    private RoleAssignment roleAssignment;
    private SyncModul syncModul;
    private ExpressionHandler expressionHandler;
    private PartialAssignmentPool pap;
    private PlanBase planBase;
    private PlanSelector planSelector;
    private AuthorityManager auth;
    private Logger log;
    private VariableSyncModule variableSyncModule;
    private IAlicaClock alicaClock;
    private IPlanner planner;
    private HashMap<Integer, Solver> solver = new HashMap<>();

    public void setIAlicaClock(IAlicaClock clock) {
        this.alicaClock = clock;
    }

    public void setCommunicator(AlicaCommunication communicator) {
        this.communicator = communicator;
    }

    public void addSolver(int identifier, CGSolver solver) {
        this.solver.put(identifier, solver);
    }

    public boolean init(SystemConfig sc, IBehaviourCreator bc, ConditionCreator cc, UtilityFunctionCreator uc, ConstraintCreator crc,
                        String roleSetName, String masterPlanName, String roleSetDir, boolean stepEngine) {

        this.maySendMessages =  !Boolean.valueOf((String) sc.get("Alica").get("Alica.SilentStart"));
        this.useStaticRoles = Boolean.valueOf((String) sc.get("Alica").get("Alica.UseStaticRoles"));
        AssignmentCollection.maxEpsCount = Short.valueOf((String) sc.get("Alica").get("Alica.MaxEpsPerPlan"));
        AssignmentCollection.allowIdling  = Boolean.valueOf((String) sc.get("Alica").get("Alica.AllowIdling"));

        this.terminating = false;
        this.stepEngine = stepEngine;

        this.sc = sc;

        if (this.planRepository == null) {
            this.planRepository = new PlanRepository();
        }
        if (this.planParser == null) {
            this.planParser = new PlanParser(this, this.planRepository);
        }

        if (this.masterPlan == null) {
            this.masterPlan = this.planParser.parsePlanTree(masterPlanName);
        }

        if (this.roleSet == null) {
            this.roleSet = this.planParser.parseRoleSet(roleSetName, roleSetDir);
        }

        if (this.behaviourPool == null) {
            this.behaviourPool = new BehaviourPool(this);
        }

        if (this.teamObserver == null) {
            this.teamObserver = new TeamObserver(this);
        }

        if (this.roleAssignment == null) {

            if (this.useStaticRoles) {
                this.roleAssignment = new StaticRoleAssignment(this);
            }
			else {
                this.roleAssignment = new RoleAssignment(this);
            }
            // the communicator is expected to be set before init() is called
            this.roleAssignment.setCommunication(communicator);
        }
        if (this.syncModul == null) {
            this.syncModul = new SyncModul(this);
        }

        if (this.expressionHandler == null) {
            this.expressionHandler = new ExpressionHandler(this, cc, uc, crc);
        }

        this.stepCalled = false;
        boolean everythingWorked = this.behaviourPool.init(bc);
        this.auth = new AuthorityManager(this);
        this.log = new Logger(this);
        this.teamObserver.init();
        this.roleAssignment.init();

        if (this.pap == null) {
            pap = new PartialAssignmentPool();
        }

        if (planSelector == null) {
            this.planSelector = new PlanSelector(this, pap);
        }

        this.auth.init();
        this.planBase = new PlanBase(this, this.masterPlan);
        this.expressionHandler.attachAll();
        UtilityFunction.initDataStructures(this);
        this.syncModul.init();

        if (this.variableSyncModule == null) {
            this.variableSyncModule = new VariableSyncModule(this);
        }

        if (this.getCommunicator() != null) {
            this.getCommunicator().init();
            this.getCommunicator().startCommunication();
        }

        if (this.variableSyncModule != null) {
            this.variableSyncModule.init();
        }

        return everythingWorked;
    }


    public void stepNotify() {
        this.setStepCalled(true);
//        this.getPlanBase().getStepModeCV().notifyAllThreads();
        this.getPlanBase().getStepModeCV().notifyOneThread();
    }


    public AlicaCommunication getCommunicator() {
        return communicator;
    }

    public void start() {
        this.planBase.start();
        System.out.println("AE: Engine started");
    }

    /**
     * Register with this EngineTrigger to be called after an engine iteration is complete.
     */
    public void iterationComplete() {
        //TODO: implement the trigger function for iteration complete
        if (CommonUtils.AE_DEBUG_debug)  CommonUtils.aboutCallNotification("implement the trigger function for iteration complete");
    }

    public Logger getLog() {
        return log;
    }

    public TeamObserver getTeamObserver() {
        return teamObserver;
    }

    public SyncModul getSyncModul() {
        return syncModul;
    }

    public AuthorityManager getAuth() {
        return auth;
    }

    public RoleAssignment getRoleAssignment() {
        return roleAssignment;
    }

    public IAlicaClock getIAlicaClock() {
        return alicaClock;
    }

    public boolean getStepEngine() {
        return stepEngine;
    }

    public void abort(String msg) {
        CommonUtils.aboutError("ABORT: " + msg);
        System.exit(CommonUtils.EXIT_FAILURE);
    }

    public void abort(String msg, String tail) {
        this.maySendMessages = false;
        String ss = "";
        ss += msg + tail;
        CommonUtils.aboutError("ABORT: " + msg);
        System.exit(CommonUtils.EXIT_FAILURE);
    }

    public void print(String msg) {
        System.out.println( msg);
    }

    public PlanRepository getPlanRepository() {
        return planRepository;
    }

    public String getAgentName() { return sc.getHostname(); }

    public RoleSet getRoleSet() {
        return roleSet;
    }

    public void setStepCalled(boolean stepCalled) {
        this.stepCalled = stepCalled;
    }

    public boolean getStepCalled() {
        return stepCalled;
    }

    public PlanBase getPlanBase() {
        return planBase;
    }

    public PlanSelector getPlanSelector() {
        return planSelector;
    }

    public PartialAssignmentPool getPartialAssignmentPool() {
        return pap;
    }

    public IPlanner getPlanner() {
        return planner;
    }

    public PlanParser getPlanParser() {
        return planParser;
    }

    public VariableSyncModule getResultStore() {
        return this.variableSyncModule;
    }

    public void setResultStore(VariableSyncModule resultStore) {
        this.variableSyncModule = resultStore;
    }

    public IBehaviourPool getBehaviourPool() {
        return behaviourPool;
    }

    public boolean isMaySendMessages() {
        return maySendMessages;
    }

    public SystemConfig getSystemConfig() {
        return sc;
    }
}
