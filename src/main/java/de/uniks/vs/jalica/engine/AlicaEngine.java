package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.authority.AuthorityManager;
import de.uniks.vs.jalica.engine.blackboard.Blackboard;
import de.uniks.vs.jalica.engine.common.SolverType;
import de.uniks.vs.jalica.engine.common.SystemConfig;
import de.uniks.vs.jalica.engine.constrainmodule.VariableSyncModule;
import de.uniks.vs.jalica.engine.expressions.ExpressionHandler;
import de.uniks.vs.jalica.engine.idmanagement.ID;
import de.uniks.vs.jalica.engine.idmanagement.IDManager;
import de.uniks.vs.jalica.engine.model.Plan;
import de.uniks.vs.jalica.engine.model.RoleSet;
import de.uniks.vs.jalica.engine.modelmanagement.ModelManager;
import de.uniks.vs.jalica.engine.planselection.PartialAssignment;
import de.uniks.vs.jalica.engine.roleassignment.DynamicRoleAssignment;
import de.uniks.vs.jalica.engine.roleassignment.StaticRoleAssignment;
import de.uniks.vs.jalica.engine.syncmodule.SyncModule;
import de.uniks.vs.jalica.engine.teammanagement.TeamManager;
import de.uniks.vs.jalica.engine.teammanagement.TeamObserver;

import java.util.HashMap;

/**
 * Created by alex on 13.07.17.
 * Updated 26.6.19
 */
public class AlicaEngine {

    private PlanBase planBase;
    private TeamObserver teamObserver;
    private ExpressionHandler expressionHandler;
    private BehaviourPool behaviourPool;
    private RoleSet roleSet;
    private VariableSyncModule variableSyncModule;
    private AuthorityManager auth;
    private TeamManager teamManager;
    private SyncModule syncModul;
    private PlanRepository planRepository;
    private Blackboard blackboard;

    private IDManager agentIDManager;
    private Logger log;
    private ModelManager modelManager;

    private IRoleAssignment roleAssignment;
    private IAlicaCommunication communicator;
    private AlicaClock alicaClock;


    private boolean maySendMessages;
    private boolean stepEngine;
    private boolean terminating;
    private boolean useStaticRoles;
    private boolean stepCalled;

    private Plan masterPlan;
    private HashMap<Integer, SolverType> solvers = new HashMap<>();
    private SystemConfig sc;

    public AlicaEngine(IDManager idManager, String roleSetName, SystemConfig sc, String masterPlanName, boolean stepEngine) {
        this.stepCalled = false;
        this.planBase = null;
        this.communicator = null;
        this.alicaClock = null;
        this.sc = sc;
        this.terminating = false;
        this.expressionHandler = null;
        this.log = null;
        this.auth = null;
        this.variableSyncModule = null;
        this.stepEngine = stepEngine;
        this.agentIDManager = idManager;
        this.maySendMessages = !Boolean.valueOf((String) sc.get("Alica").get("Alica.SilentStart"));
        this.useStaticRoles = Boolean.valueOf((String) sc.get("Alica").get("Alica.UseStaticRoles"));
        PartialAssignment.allowIdling(Boolean.valueOf((String) sc.get("Alica").get("Alica.AllowIdling")));

        this.planRepository = new PlanRepository();
        this.modelManager = new ModelManager(this.planRepository, this);
        this.masterPlan = this.modelManager.loadPlanTree(masterPlanName);
        this.roleSet = this.modelManager.loadRoleSet(roleSetName);

        this.teamManager = new TeamManager(this, true);
        this.teamManager.init();

        this.behaviourPool = new BehaviourPool(this);
        this.teamObserver = new TeamObserver(this);
        if (this.useStaticRoles) {
            this.roleAssignment = new StaticRoleAssignment(this);
        } else {
            if(CommonUtils.AE_DEBUG_debug)CommonUtils.aboutCallNotification("DynamicRoleAssignment!");
            this.roleAssignment = new DynamicRoleAssignment(this);
        }

        // the communicator is expected to be set before init() is called
        this.roleAssignment.setCommunication(communicator);
        this.syncModul = new SyncModule(this);

        if (!planRepository.verifyPlanBase()) {
            CommonUtils.aboutError("Error in parsed plans.");
        }
        System.out.println("AE: Constructor finished!");
    }

    public boolean init(IBehaviourCreator bc, IConditionCreator cc, IUtilityCreator uc, IConstraintCreator crc) {
        if (this.expressionHandler == null) {
            this.expressionHandler = new ExpressionHandler(this, cc, uc, crc);
        }

        this.stepCalled = false;
        boolean everythingWorked = true;
        everythingWorked &= this.behaviourPool.init(bc);
        this.auth = new AuthorityManager(this);
        this.log = new Logger(this);
        this.roleAssignment.init();
        this.auth.init();
        this.planBase = new PlanBase(this, this.masterPlan);

        this.expressionHandler.attachAll();
        UtilityFunction.initDataStructures(this);
        this.syncModul.init();
        if (this.variableSyncModule == null) {
            this.variableSyncModule = new VariableSyncModule(this);
        }
        if (this.communicator != null) {
            this.communicator.startCommunication();
        }
        if (this.variableSyncModule != null) {
            this.variableSyncModule.init();
        }
        RunningPlan.init(this.sc);
        return everythingWorked;
    }

    public void shutdown() {
        if (this.communicator != null) {
            this.communicator.stopCommunication();
        }
        this.terminating = true;
        this.maySendMessages = false;

        if (this.behaviourPool != null) {
            this.behaviourPool.stopAll();
            this.behaviourPool.terminateAll();
            this.behaviourPool = null;
        }
        if (this.planBase != null) {
            this.planBase.stop();
            this.planBase = null;
        }
        if (this.auth != null) {
            this.auth.close();
            this.auth = null;
        }
        if (this.syncModul != null) {
            this.syncModul.close();
            this.syncModul = null;
        }
        if (this.teamObserver != null) {
            this.teamObserver.close();
            this.teamObserver = null;
        }
        if (this.log != null) {
            this.log.close();
            this.log = null;
        }
        if (this.planRepository != null) {
            this.planRepository = null;
        }
        if (this.modelManager != null) {
            this.modelManager = null;
        }
        this.roleSet = null;
        this.masterPlan = null;

        if (this.expressionHandler != null) {
            this.expressionHandler = null;
        }
        if (this.variableSyncModule != null) {
            this.variableSyncModule = null;
        }
        if (this.roleAssignment != null) {
            this.roleAssignment = null;
        }
        alicaClock = null;
    }

    public void iterationComplete()
    {
        // TODO: implement the trigger function for iteration complete
    }

    public void start() {
        this.planBase.start();
        System.out.println("AE: Engine started");
    }

    public void setStepCalled(boolean stepCalled) {
        this.stepCalled = stepCalled;
    }

    public boolean getStepCalled() {
        System.out.println("AE: step called " + this.stepCalled);
        return this.stepCalled;
    }

    public boolean getStepEngine() {
        return this.stepEngine;
    }

    public void setAlicaClock(AlicaClock clock) {
        this.alicaClock = clock;
    }

    public void setTeamObserver(TeamObserver teamObserver)
    {
        this.teamObserver = teamObserver;
    }

    public void setSyncModul(SyncModule syncModul)
    {
        this.syncModul = syncModul;
    }

    public void setAuth(AuthorityManager auth)
    {
        this.auth = auth;
    }

    public void setRoleAssignment(IRoleAssignment roleAssignment)
    {
        this.roleAssignment = roleAssignment;
    }

    public void setStepEngine(boolean stepEngine)
    {
        this.stepEngine = stepEngine;
    }

    public String getRobotName()
    {
        return sc.getHostname();
    }

    public void setLog(Logger log)
    {
        this.log = log;
    }

    public boolean isTerminating()
    {
        return terminating;
    }

    public void setMaySendMessages(boolean maySendMessages)
    {
        this.maySendMessages = maySendMessages;
    }

    public void setCommunicator(IAlicaCommunication communicator)
    {
        this.communicator = communicator;
    }

    public void setResultStore(VariableSyncModule resultStore)
    {
        this.variableSyncModule = resultStore;
    }

    public void stepNotify() {
        this.setStepCalled(true);
        this.getPlanBase().getStepModeCV().notifyAllThreads();
    }

    public long getIDFromBytes( byte idBytes, int idSize, byte type) {
        return this.agentIDManager.getIDFromBytes(idBytes, idSize, type);
    }


    public boolean maySendMessages()  { return this.maySendMessages; }

    // Module Access:
    public AuthorityManager  getAuth()  { return this.auth; }
    public BehaviourPool  getBehaviourPool()  { return this.behaviourPool; }
    public IAlicaCommunication  getCommunicator()  { return this.communicator; }
    public Logger  getLog()  { return this.log; }
    public PlanBase  getPlanBase()  { return this.planBase; }
//    PlanParser  getPlanParser()  { return planParser; }
    public ModelManager  getModelManager()  { return this.modelManager; }
    public PlanRepository  getPlanRepository()  { return this.planRepository; }
    public VariableSyncModule  getResultStore()  { return this.variableSyncModule; }
    public IRoleAssignment  getRoleAssignment()  { return this.roleAssignment; }
    public SyncModule  getSyncModul()  { return this.syncModul; }
    public TeamManager  getTeamManager()  { return this.teamManager; }
    public TeamObserver  getTeamObserver()  { return this.teamObserver; }
    public AlicaClock  getAlicaClock()  { return this.alicaClock; }
    public Blackboard getBlackBoard()  { return this.blackboard; }
    public Blackboard editBlackBoard() { return this.blackboard; }
    public RoleSet  getRoleSet()  { return roleSet; }
    public SystemConfig getSystemConfig() {
        return sc;
    }

    public void addSolver(int identifier, SolverType solver) {
        this.solvers.put(identifier, solver);
    }

    public SolverType getSolver() {
        return this.solvers.get(SolverType.class.hashCode());
    }

    public ID getId(long id) {
        return agentIDManager.getOrGenerateUniqueID(id);
    }

    public ID getId(String id) {
        return agentIDManager.getOrGenerateUniqueID(id);
    }

    //    public boolean init(SystemConfig sc, IBehaviourCreator bc, IConditionCreator cc, IUtilityFunctionCreator uc, IConstraintCreator crc,
//                        String roleSetName, String masterPlanName, String roleSetDir, boolean stepEngine) {
//
//        this.maySendMessages    =  !Boolean.valueOf((String) sc.get("Alica").get("Alica.SilentStart"));
//        this.useStaticRoles     = Boolean.valueOf((String) sc.get("Alica").get("Alica.UseStaticRoles"));
//        AssignmentCollection.maxEpsCount   = Short.valueOf((String) sc.get("Alica").get("Alica.MaxEpsPerPlan"));
//        AssignmentCollection.allowIdling  = Boolean.valueOf((String) sc.get("Alica").get("Alica.AllowIdling"));
//        this.terminating = false;
//        this.stepEngine = stepEngine;
//        this.systemConfig = sc;
//
//        this.planRepository = new PlanRepository();
//        this.modelManager = new ModelManager(this.planRepository,this);
////        this.planParser = new PlanParser(this, this.planRepository);
//        this.masterPlan = this.modelManager.parsePlanTree(masterPlanName);
//        this.roleSet = this.modelManager.parseRoleSet(roleSetName, roleSetDir);
//
//        this.teamManager = new TeamManager(this, true);
//        this.teamManager.init();
//
//        this.behaviourPool = new BehaviourPool(this);
//        this.teamObserver = new TeamObserver(this);
//
//        if (this.useStaticRoles)
//            this.roleAssignment = new StaticRoleAssignment(this);
//        else
//            this.roleAssignment = new DynamicRoleAssignment(this);
//
//        this.roleAssignment.setCommunication(communicator);
//        this.syncModul = new SyncModule(this);
//
//        if (!planRepository.verifyPlanBase())
//            abort("Error in parsed plans.");
//
//        // -------- new init --------------
//        if (this.expressionHandler == null)
//            this.expressionHandler = new ExpressionHandler(this, cc, uc, crc);
//
//        this.stepCalled = false;
//        boolean everythingWorked = this.behaviourPool.init(bc);
//
//        this.authorityManager = new AuthorityManager(this);
//        this.logger = new Logger(this);
////        this.teamObserver.init();
//        this.roleAssignment.init();
//
////        if (this.assignmentPool == null)
////            assignmentPool = new PartialAssignmentPool(10100);
//
////        if (planSelector == null)
////            this.planSelector = new PlanSelector(this, planBase, assignmentPool);
//
//        this.authorityManager.init();
//
//        this.planBase = new PlanBase(this, this.masterPlan);
////        this.planSelector.setPlanBase(this.planBase);
//
//        this.expressionHandler.attachAll();
//        UtilityFunction.initDataStructures(this);
//        this.syncModul.init();
//
//        if (this.variableSyncModule == null)
//            this.variableSyncModule = new VariableSyncModule(this);
//
//        if (this.getCommunicator() != null) {
////            this.getCommunicator().init(this.teamObserver.getAvailableAgentIDs());
//            this.getCommunicator().init();
//            this.getCommunicator().startCommunication();
//        }
//
//        if (this.variableSyncModule != null)
//            this.variableSyncModule.init();
//        RunningPlan.init(sc);
//        return everythingWorked;
//    }
//
//
//    public void stepNotify() {
//
//        if (!this.stepEngine)
//            return;
//        this.setStepCalled(true);
//        if (CommonUtils.AE_DEBUG_debug) System.out.println("AE: stepNotify");
//        this.getPlanBase().getStepModeCV().notifyOneThread();
////        this.getPlanBase().getStepModeCV().notifyAllThreads();
//    }
//
//
//    public IAlicaCommunication getCommunicator() {
//        return communicator;
//    }
//
//    public void start() {
//        if (CommonUtils.AE_DEBUG_debug)  System.out.println("AE: ------------------ Engine started ------------------");
//        this.planBase.start();
//    }
//
//    /**
//     * Register with this EngineTrigger teamObserver be called after an engine iteration is complete.
//     */
//    public void iterationComplete() {
//        //TODO: implement the trigger function for iteration complete
//        if (CommonUtils.AE_DEBUG_debug)  CommonUtils.aboutCallNotification("implement the trigger function for iteration complete");
//    }
//
//    public Logger getLogger() {
//        return logger;
//    }
//
//    public TeamObserver getTeamObserver() {
//        return teamObserver;
//    }
//
//    public SyncModule getSyncModul() {
//        return syncModul;
//    }
//
//    public AuthorityManager getAuth() {
//        return authorityManager;
//    }
//
//    public IRoleAssignment getRoleAssignment() {
//        return roleAssignment;
//    }
//
//    public AlicaClock getAlicaClock() {
//        return alicaClock;
//    }
//
//    public boolean getStepEngine() {
//        return stepEngine;
//    }
//
//    public void shutDown() { CommonUtils.aboutNoImpl(); }
//
//    public static void abort(String msg) {
//        CommonUtils.aboutError("ABORT: " + msg);
//        System.exit(CommonUtils.EXIT_FAILURE);
//    }
//
//    public void abort(String msg, String tail) {
//        this.maySendMessages = false;
//        String ss = "";
//        ss += msg + tail;
//        CommonUtils.aboutError("ABORT: " + msg);
//        System.exit(CommonUtils.EXIT_FAILURE);
//    }
//
//    public void print(String msg) {
//        System.out.println( msg);
//    }
//
//    public PlanRepository getPlanRepository() {
//        return planRepository;
//    }
//
//    public String getAgentName() { return systemConfig.getHostname(); }
//
//    public RoleSet getRoleSet() {
//        return roleSet;
//    }
//
//    public void setStepCalled(boolean stepCalled) {
//        this.stepCalled = stepCalled;
//    }
//
//    public boolean getStepCalled() {
//        return stepCalled;
//    }
//
//    public PlanBase getPlanBase() {
//        return planBase;
//    }
//
//
////    public PlanSelector getPlanSelector() {
////        return planSelector;
////    }
////
////    public PartialAssignmentPool getPartialAssignmentPool() {
////        return assignmentPool;
////    }
//
////    public IPlanner getPlanner() {
////        return planner;
////    }
////
////    public PlanParser getPlanParser() {
////        return planParser;
////    }
//    public VariableSyncModule getResultStore() {
//        return this.variableSyncModule;
//    }
//
//    public void setResultStore(VariableSyncModule resultStore) {
//        this.variableSyncModule = resultStore;
//    }
//
//    public IBehaviourPool getBehaviourPool() {
//        return behaviourPool;
//    }
//
//    public boolean isMaySendMessages() {
//        return maySendMessages;
//    }
//
//    public SystemConfig getSystemConfig() {
//        return systemConfig;
//    }
//
//    public TeamManager getTeamManager() { return teamManager; }
//
//    public ModelManager getModelManager() {
//        return modelManager;
//    }
//
//    public Logger getLog() {
//        return logger;
//    }
//
//    public boolean maySendMessages( ) {
//        return this.maySendMessages ;
//    }
//
//    //TODO: check
//
//    public long getID(int id) {
//        CommonUtils.aboutCallNotification("AlicaEngine:getid " + id);
//        return IDManager.generateUniqueID(String.valueOf(id));
//    }
//    public void setAlicaClock(AlicaClock clock) {
//        this.alicaClock = clock;
//    }
//
//    public void setCommunicator(IAlicaCommunication communicator) {
//        this.communicator = communicator;
//    }




}
