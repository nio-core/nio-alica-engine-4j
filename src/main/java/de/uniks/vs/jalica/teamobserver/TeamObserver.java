package de.uniks.vs.jalica.teamobserver;

import de.uniks.vs.jalica.common.config.ConfigPair;
import de.uniks.vs.jalica.common.Logger;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.unknown.*;

import java.util.*;

/**
 * Created by alex on 13.07.17.
 */
public class TeamObserver implements ITeamObserver {

    private Logger log;
    private HashMap<Long, SimplePlanTree> simplePlanTrees;
    private AgentEngineData me;
    private AlicaEngine ae;
    private ArrayList<AgentEngineData> allOtherAgents;
    private long teamTimeOut;
    private long myId;
    private Set<Long> ignoredAgents = new HashSet<>();
    private AgentProperties ownAgentProperties;
    private ArrayList<AgentProperties> availableAgentProperties = new ArrayList<>();

    public TeamObserver(AlicaEngine ae) {
        this.ae = ae;
        this.teamTimeOut = 0;
        this.myId = 0;
        this.me = null;
        this.log = null;
        this.simplePlanTrees = new HashMap<>();
        this.allOtherAgents = new ArrayList<>();
    }

    public void init() {
        this.log = ae.getLogger();
        this.myId = 0;
        this.me = null;
        String ownPlayerName = ae.getAgentName();
        System.out.println( "TO: Initing Agent:" + ownPlayerName );
        this.teamTimeOut = Long.valueOf((String) this.ae.getSystemConfig().get("Alica").get("Alica.TeamTimeOut")) * 1000000;
        Vector<String> playerNames =((ConfigPair)this.ae.getSystemConfig().get("Globals").get("Team")).getKeys();
        boolean foundSelf = false;

        for (int i = 0; i < playerNames.size(); i++) {
            AgentProperties rp = new AgentProperties(ae, playerNames.get(i));

            if (!foundSelf && playerNames.get(i).equals(ownPlayerName)) {
                foundSelf = true;
                this.me = new AgentEngineData(ae, rp);
                this.me.setActive(true);
                this.myId = rp.getID();
            }
            else {

                for (AgentEngineData red : this.allOtherAgents) {

                    if (red.getProperties().getID() == rp.getID()) {
                        String ss;
                        ss = "TO: Found twice Agent ID " + rp.getID() + " in globals team section" + "\n";
                        ae.abort(ss);
                    }
                    if (rp.getID() == myId) {
                        String ss2;
                        ss2 = "TO: Found myself twice Agent ID " + rp.getID() + " in globals team section" + "\n";
                        ae.abort(ss2);
                    }
                }
                this.allOtherAgents.add(new AgentEngineData(ae, rp));
            }
        }

        if (!foundSelf) {
            ae.abort("TO: Could not find own agent name in Globals Id = " + ownPlayerName);
        }

        if (Boolean.valueOf((String) this.ae.getSystemConfig().get("Alica").get("Alica.TeamBlackList.InitiallyFull"))) {

            for (AgentEngineData r : this.allOtherAgents) {
                this.ignoredAgents.add(r.getProperties().getID());
            }
        }
    }

    public AgentProperties getOwnAgentProperties() {
        return this.me.getProperties();
    }

    @Override
    public int teamSize() {
        int count = 1;

        for (AgentEngineData r : this.allOtherAgents){

            if (r.isActive()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void handlePlanTreeInfo(PlanTreeInfo incoming) {

        if (incoming.senderID != myId) {

            if (isAgentIgnored(incoming.senderID)) {
                System.out.println("TO("+this.getOwnID()+"): agent " + incoming.senderID + " ignored" );
                return;
            }

            SimplePlanTree spt = sptFromMessage(incoming.senderID, incoming.stateIDs);

            if (spt != null) {

                for (AgentEngineData agentEngineData : allOtherAgents) {
                    System.out.println("TO("+this.getOwnID()+"): agent " + agentEngineData.getProperties().getID());
                    if (agentEngineData.getProperties().getID() == incoming.senderID) {

                        synchronized (agentEngineData) {
                            agentEngineData.setLastMessageTime(ae.getIAlicaClock().now().time);
                            agentEngineData.setSuccessMarks(new SuccessMarks(ae, incoming.succeededEPs));
                            System.out.println("TO("+this.getOwnID()+"): agent " + incoming.senderID + "  succeeded EPs " + incoming.succeededEPs );
                        }
                    }
                    break;
                }
            }
            synchronized (this.simplePlanTrees) {
                this.simplePlanTrees.put(incoming.senderID, spt);
                if (CommonUtils.TO_DEBUG_debug) System.out.println("TO("+this.getOwnID()+"): Plan tree size: " +this.simplePlanTrees.size());
            }
        }
    }


    /**
     * Constructs a SimplePlanTree from a received message
     * @param agentID The id of the other robot.
     * @param ids The list of long encoding another robot's plantree as received in a PlanTreeInfo message.
     * @return shared_ptr of a SimplePlanTree
     */
    private SimplePlanTree sptFromMessage(long agentID, ArrayList<Long> ids) {

        if (CommonUtils.TO_DEBUG_debug) {
            System.out.println("TO("+this.getOwnID()+"): Spt from robot " + agentID);

            for (long i : ids) {
                System.out.println(i + "\t");
            }
            System.out.println();
        }

        // TODO: Refactor the complete following code
        if (ids.size() == 0) {
            //warning
            System.err.println("TO("+this.getOwnID()+"): Empty state list for robot " + agentID);
            return null;
        }
        LinkedHashMap<Long, State> states = ae.getPlanRepository().getStates();
        double time = ae.getIAlicaClock().now().time;
        SimplePlanTree root = new SimplePlanTree();
        root.setAgentID(agentID);
        root.setReceiveTime(time);
        root.setStateIds(ids);
        State s = null;

        long id0 = ids.get(0);

        if (states.containsKey(id0)) {
            root.setState(states.get(id0));
            root.setEntryPoint(entryPointOfState(root.getState()));

            if (root.getEntryPoint() == null) {
                //Warning
                System.err.println("TO("+this.getOwnID()+"): Cannot find ep for State (" + ids.get(0) + ") received from " + agentID);
                return null;
            }
        }
		else {
            System.err.println("TO("+this.getOwnID()+"): Unknown State (" + ids.get(0) + ") received from " + agentID);
            return null;
        }
        SimplePlanTree curParent = null;
        SimplePlanTree cur = root;

        if (ids.size() > 1) {
//            list<long>::const_iterator iter = ids.begin();
//            iter++;
//            for (; iter != ids.end(); iter++)

            for(int i = 1; i < ids.size(); i++) {
                long id = ids.get(i);

                if (id == -1) {
                    curParent = cur;
                    cur = null;
                }
				else if (id == -2) {
                    cur = curParent;

                    if (cur == null) {
                        System.err.println( "TO("+this.getOwnID()+"): Malformed SptMessage from " + agentID);
                        return null;
                    }
                }
				else {
                    cur = new SimplePlanTree();
                    cur.setAgentID(agentID);
                    cur.setReceiveTime(time);
                    curParent.getChildren().add(cur);

                    if (states.containsKey(id)) {
                        cur.setState(states.get(id));
                        cur.setEntryPoint(entryPointOfState(cur.getState()));

                        if (cur.getEntryPoint() == null) {
                            System.err.println("TO("+this.getOwnID()+"): Cannot find ep for State (" + ids.get(0) + ") received from " + agentID);
                            return null;
                        }
                    }
					else {
                        System.err.println("TO("+this.getOwnID()+"): Unknown State (" + ids.get(0) + ") received from " + agentID );
                        return null;
                    }
                }
            }
        }

        return root;
    }

    private EntryPoint entryPointOfState(State state) {

        for (EntryPoint entryPoint : state.getInPlan().getEntryPoints().values()) {

            if (entryPoint.getReachableStates().contains(state)) {
                return entryPoint;
            }
        }
        return null;
    }

    public boolean isAgentIgnored(long senderID) {
        return ignoredAgents.contains(senderID);
    }

    @Override
    public void updateSuccessCollection(Plan p, SuccessCollection sc) {
        sc.clear();
        ArrayList<EntryPoint > suc = new ArrayList<>();

        for (AgentEngineData r : this.allOtherAgents) {

            if (r.isActive()) {
//                    lock_guard<mutex> lock(this.successMark);
                suc = r.getSuccessMarks().succeededEntryPoints(p);

                if (suc != null) {

                    for (EntryPoint ep : suc) {
                        sc.setSuccess(r.getProperties().getID(), ep);
                    }
                }
            }
        }
        suc = me.getSuccessMarks().succeededEntryPoints(p);

        if (suc != null)
        {
            for (EntryPoint ep : suc){
                sc.setSuccess(myId, ep);
            }
        }
    }

    public ArrayList<AgentProperties> getAvailableAgentProperties() {
        ArrayList<AgentProperties> ret = new ArrayList<>();
        ret.add(me.getProperties());

        for (AgentEngineData r : this.allOtherAgents)
        {
            if (r.isActive())
            {
                ret.add(r.getProperties());
            }
        }
        return ret;
    }

    public void notifyAgentLeftPlan(AbstractPlan plan) {

//        lock_guard<mutex> lock(this.simplePlanTreeMutex);

        for (SimplePlanTree planTree : this.simplePlanTrees.values()) {

            if (planTree.containsPlan(plan)) {
                return;
            }

        }
        this.me.getSuccessMarks().removePlan(plan);
    }

    @Override
    public ArrayList<Long> getAvailableAgentIDs() {
        ArrayList<Long> ret = new ArrayList<>();
        ret.add(myId);
        for (AgentEngineData r : this.allOtherAgents)
        {
            if (r.isActive())
            {
                ret.add(r.getProperties().getID());
            }
        }
        return CommonUtils.move(ret);
    }

    @Override
    public int successesInPlan(Plan plan) {
        int ret = 0;
        List<EntryPoint> suc = new ArrayList<EntryPoint>();

        for (AgentEngineData r : this.allOtherAgents) {

            if (r.isActive()) {
                {
//                    lock_guard<mutex> lock(this.successMark);
                    suc = r.getSuccessMarks().succeededEntryPoints(plan);
                }

                if (suc != null) {
                    ret += suc.size();
                }
            }
        }
        suc = me.getSuccessMarks().succeededEntryPoints(plan);

        if (suc != null) {
            ret += suc.size();
        }
        return ret;
    }

    @Override
    public LinkedHashMap<Long, SimplePlanTree> getTeamPlanTrees() {
        LinkedHashMap<Long, SimplePlanTree> ret = new LinkedHashMap<Long, SimplePlanTree> ();
//        lock_guard<mutex> lock(this.simplePlanTreeMutex);
        for (AgentEngineData r : this.allOtherAgents) {

            if (r.isActive()) {

//                map<int, shared_ptr<SimplePlanTree> >::iterator iter = this.simplePlanTrees.find(r.getProperties().getID());

                SimplePlanTree planTree = this.simplePlanTrees.get(r.getProperties().getID());

                if (planTree != null) {

                    ret.put(r.getProperties().getID(), planTree);
                }
            }
        }
        return ret;
    }

    @Override
    public SuccessCollection getSuccessCollection(Plan plan) {
        SuccessCollection ret = new SuccessCollection(plan);
        ArrayList<EntryPoint> suc = new ArrayList<>();
        for (AgentEngineData r : this.allOtherAgents)
        {
            if (r.isActive())
            {
                {
//                    lock_guard<mutex> lock(this.successMark);
                    suc = r.getSuccessMarks().succeededEntryPoints(plan);
                }
                if (suc != null)
                {
                    for (EntryPoint ep : suc)
                    {
                        ret.setSuccess(r.getProperties().getID(), ep);
                    }
                }
            }
        }
        suc = me.getSuccessMarks().succeededEntryPoints(plan);
        if (suc != null)
        {
            for (EntryPoint ep : suc)
            {
                ret.setSuccess(myId, ep);
            }
        }
        return ret;
    }

    @Override
    public long getOwnID() {
        return myId;
    }

    @Override
    public void tick(RunningPlan root) {
        boolean changed = false;
        AlicaTime time = ae.getIAlicaClock().now();
        Vector<Long> agentsAvail = new Vector<>();
        agentsAvail.add(this.myId);

        for (AgentEngineData r : this.allOtherAgents) {

            if ((r.getLastMessageTime() + teamTimeOut) < time.time) {
                changed |= r.isActive();
                r.setActive(false);
                r.getSuccessMarks().clear();
//                lock_guard<mutex> lock(this.simplePlanTreeMutex);
                this.simplePlanTrees.remove(r.getProperties().getID());
            }
            else if (!r.isActive()) {
                r.setActive(true);
                changed = true;
            }

            if (r.isActive()) {
                agentsAvail.add(r.getProperties().getID());
            }
        }

        // notifications for teamchanges, you can add some code below if you want to be notified when the team changed
        if (changed) {
            ae.getRoleAssignment().update();
            this.log.eventOccured("TeamChanged");
        }
        cleanOwnSuccessMarks(root);

        if (root != null) {
            ArrayList<SimplePlanTree>  updatePlanTrees = new ArrayList<>();
            ArrayList<Long> noUpdates = new ArrayList<>();
//            lock_guard<mutex> lock(this.simplePlanTreeMutex);
//            for (auto iterator = this.simplePlanTrees.begin(); iterator != this.simplePlanTrees.end(); iterator++) {
            for (long key : this.simplePlanTrees.keySet()) {
                SimplePlanTree second = this.simplePlanTrees.get(key);

                if (agentsAvail.contains(second.getAgentID())) {

                    if (second.isNewSimplePlanTree())
                    {
                        updatePlanTrees.add(second);
                        if (CommonUtils.TO_DEBUG_debug)  System.out.println( "TO("+this.getOwnID()+"): added to update");
                        second.setNewSimplePlanTree(false);
                    }
                    else
                    {
                        if (CommonUtils.TO_DEBUG_debug)  System.out.println("TO: added to noupdate" );
                        noUpdates.add(second.getAgentID());
                    }
                }
            }
            if (CommonUtils.TO_DEBUG_debug)  System.out.println("TO: spts size " + updatePlanTrees.size());

            if (root.recursiveUpdateAssignment(updatePlanTrees, agentsAvail, noUpdates, time))
            {
                this.log.eventOccured("MsgUpdate");
            }
        }
    }

    /**
     * Broadcasts a PlanTreeInfo Message
     * @param msg A list of long, a serialized version of the current planning tree
     * as constructed by RunningPlan.ToMessage.
     */
    @Override
    public void doBroadCast(ArrayList<Long> msg) {
        if (!ae.isMaySendMessages()) {
            return;
        }
        PlanTreeInfo planTreeInfo = new PlanTreeInfo();
        planTreeInfo.senderID = this.myId;
        planTreeInfo.stateIDs = msg;
        planTreeInfo.succeededEPs = this.getOwnEngineData().getSuccessMarks().toList();
        ae.getCommunicator().sendPlanTreeInfo(planTreeInfo);
//#ifdef TO_DEBUG
        String ss = "TO("+this.getOwnID()+"): Sending Plan Message: " +"\n";

        for (long i: msg) {
            ss+= "    " +i + "\t";
        }

        ss+="\n";
        if (CommonUtils.TO_DEBUG_debug)  System.out.println(ss);
//#endif
    }

    @Override
    public AgentEngineData getOwnEngineData() {
        return this.me;
    }

    public AgentEngineData getAgentById(long id) {

        if (id == myId) {
            return this.me;
        }

        for (AgentEngineData r : this.allOtherAgents) {

            if (r.getProperties().getID() == id) {
                return r;
            }
        }
        return null;
    }

    private void cleanOwnSuccessMarks(RunningPlan root) {
        HashSet<AbstractPlan> presentPlans  = new HashSet<AbstractPlan>();
        if (root != null) {
            ArrayList<RunningPlan> q = new ArrayList<RunningPlan>();
            q.add(0,root);

            while (q.size() > 0) {
                RunningPlan p = q.get(0);
                q.remove(0);

                if (!p.isBehaviour()) {
                    presentPlans.add(p.getPlan());

                    for (RunningPlan c : p.getChildren()) {
                        q.add(c);
                    }
                }
            }
        }
        ArrayList<SimplePlanTree > queue = new ArrayList<>();
//        lock_guard<mutex> lock(this.simplePlanTreeMutex);
        for (long key : this.simplePlanTrees.keySet())
        {

//          TODO:  if (pair.second.operator bool())
            CommonUtils.aboutImplIncomplete();
            if (this.simplePlanTrees.get(key) == null) {
                queue.add(this.simplePlanTrees.get(key));
            }
        }
        while (queue.size() > 0)
        {
            SimplePlanTree spt = queue.get(0);
            queue.remove(0);
            presentPlans.add(spt.getState().getInPlan());
            for (SimplePlanTree c : spt.getChildren())
            {
                queue.add(c);
            }
        }
//        this.getOwnEngineData().getSuccessMarks().limitToPlans(move(presentPlans));
        this.getOwnEngineData().getSuccessMarks().limitToPlans(presentPlans);
    }

    public void messageRecievedFrom(long rID) {

        for (AgentEngineData re : this.allOtherAgents) {

            if (re.getProperties().getID() == rID) {
                re.setLastMessageTime(ae.getIAlicaClock().now().time);
                break;
            }
        }

    }
}

