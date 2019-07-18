package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.common.ExtArrayList;
import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.collections.SuccessCollection;
import de.uniks.vs.jalica.engine.common.Pair;
import de.uniks.vs.jalica.engine.containers.messages.AllocationAuthorityInfo;
import de.uniks.vs.jalica.engine.idmanagement.ID;
import de.uniks.vs.jalica.engine.model.EntryPoint;
import de.uniks.vs.jalica.engine.model.Plan;
import de.uniks.vs.jalica.engine.model.State;
import de.uniks.vs.jalica.engine.planselection.PartialAssignment;
import de.uniks.vs.jalica.engine.teammanagement.view.AgentsInStateView;
import de.uniks.vs.jalica.engine.teammanagement.view.AllAgentsView;
import de.uniks.vs.jalica.engine.teammanagement.view.AssignmentSuccessView;
import de.uniks.vs.jalica.engine.teammanagement.view.AssignmentView;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Created by alex on 17.07.17.
 * update 21.6.19
 */
public class Assignment {
    Plan plan;
    ExtArrayList<AgentStatePairs> assignmentData;
    SuccessCollection successData;
    double lastUtility;

    public Assignment() {
        this.plan = null;
        this.assignmentData = new ExtArrayList<>();
        this.successData = new SuccessCollection();
        this.lastUtility = 0.0;
    }

    public Assignment(Plan p) {
        this.plan = p;
        this.assignmentData = new ExtArrayList<>(AgentStatePairs::new, p.getEntryPoints().size());
        this.successData = new SuccessCollection(p);
        this.lastUtility = 0.0;
    }

    public Assignment(PartialAssignment pa) {
        this.plan = pa.getPlan();
        this.assignmentData = new ExtArrayList<>(AgentStatePairs::new, pa.getPlan().getEntryPoints().size());
        this.successData = pa.getSuccessData();
        this.lastUtility = pa.getUtility().getMax();

        int numEps = this.plan.getEntryPoints().size();

        for (int i = 0; i < numEps; ++i) {
            this.assignmentData.get(i).reserve(pa.getAssignedAgentCount(i));
        }

        for (int i = 0; i < pa.getTotalAgentCount(); i++) {
            int idx = pa.getEntryPointIndexOf(i);

            if (idx >= 0 && idx < numEps) {
                this.assignmentData.get(idx).add(pa.getProblem().getAgents().get(i), this.plan.getEntryPoints().get(idx).getState());
            }
        }
    }

    public Assignment(Plan p, AllocationAuthorityInfo aai) {
        this.plan = p;
        this.assignmentData = new ExtArrayList<>(AgentStatePairs::new, p.getEntryPoints().size());
        this.successData = new SuccessCollection(p);
        this.lastUtility = 0.0;

        int numEps = this.plan.getEntryPoints().size();

        for (int i = 0; i < numEps; ++i) {
            assert (p.getEntryPoints().get(i).getID() == aai.entryPointAgents.get(i).entrypoint);
            this.assignmentData.get(i).getData().ensureCapacity(aai.entryPointAgents.get(i).agents.size());

            for (ID agent : aai.entryPointAgents.get(i).agents) {
                this.assignmentData.get(i).add(agent, this.plan.getEntryPoints().get(i).getState());
            }
        }
    }

    public boolean isValid() {

        if (this.plan == null) {
            return false;
        }
        ArrayList<EntryPoint> eps = this.plan.getEntryPoints();
        int numEps = eps.size();

        for (int i = 0; i < numEps; i++) {
            int c = this.assignmentData.get(i).size() + this.successData.getData().get(i).size(); // TODO: successData init with size ExtArrayList

            //TODO: reduce
            if (! eps.get(i).getCardinality().contains(c)) {
                return false;
            }
        }
        return true;
    }

    boolean isSuccessful() {

        if (this.plan == null) {
            return false;
        }
        ArrayList<EntryPoint> eps = this.plan.getEntryPoints();
        int numEps = eps.size();
        boolean ret = false;

        for (int i = 0; i < numEps; ++i) {
            if (eps.get(i).isSuccessRequired()) {
                if (this.successData.getData().get(i).isEmpty() || this.successData.getData().get(i).size() < eps.get(i).getMinCardinality()) {
                    return false;
                }
                ret = true; // Only a plan with successRequired can succeed.
            }
        }
        return ret;
    }

    boolean isAnyTaskSuccessful() {

        if (this.plan == null) {
            return false;
        }
        ArrayList<EntryPoint> eps = this.plan.getEntryPoints();
        int numEps = eps.size();

        for (int i = 0; i < numEps; ++i) {

            if (!this.successData.getData().get(i).isEmpty() && this.successData.getData().get(i).size() >= eps.get(i).getMinCardinality()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAgent(ID id) {

        for (AgentStatePairs asps : this.assignmentData) {

            if (asps.hasAgent(id)) {
                return true;
            }
        }
        return false;
    }

    public EntryPoint getEntryPointOfAgent(ID id) {
        int i = 0;
        for (AgentStatePairs asps : this.assignmentData) {

            if (asps.hasAgent(id)) {
                return this.plan.getEntryPoints().get(i);
            }
            i++;
        }
        return null;
    }

    public State getStateOfAgent(ID id) {

        for (AgentStatePairs asps : this.assignmentData) {
            State s = asps.getStateOfAgent(id);

            if (s != null) {
                return s;
            }
        }
        return null;
    }

    public void getAllAgents(ArrayList<ID> oAgents) {
//        std::transform(asps.begin(), asps.end(), std::back_inserter(o_agents), [](AgentStatePair asp) -> essentials::IdentifierConstPtr { return asp.first; });

        for (AgentStatePairs asps : this.assignmentData) {

            for (Pair<ID, State> asp : asps.getData()) {

                if (!oAgents.contains(asp.fst))
                    oAgents.add(asp.fst);
            }
        }
    }


    private ArrayList<ID> getAgentsInState(int sid) {
        State s = this.plan.getStateByID(sid);
        return s != null ? agentsInStateView(this, s) : new ArrayList<>();
    }

    private ArrayList<ID> agentsInStateView(Assignment assignment, State state) {
        AgentStatePairs asp = assignment.getAgentStates(state.getEntryPoint());
        return asp.getAgentsInState(state);
    }

    AgentStatePairs getAgentStates(EntryPoint ep) {
        return this.assignmentData.get(ep.getIndex());
    }

    public void getAgentsWorking(EntryPoint ep, ArrayList<ID> oAgents) {
        AgentStatePairs asps = getAgentStates(ep);
        oAgents.ensureCapacity(asps.size());
//        std::transform(asp.begin(), asp.end(), std::back_inserter(o_agents), [](AgentStatePair asp) -> essentials::IdentifierConstPtr { return asp.first; });
        for (Pair<ID, State> asp : asps.getData()) {

            if (!oAgents.contains(asp.fst))
                oAgents.add(asp.fst);
        }
    }

    public void getAgentsWorking(int idx, ArrayList<ID> oAgents) {
        AgentStatePairs asps = getAgentStates(idx);
        oAgents.ensureCapacity(asps.size());
//        std::transform(asp.begin(), asp.end(), std::back_inserter(o_agents), [](AgentStatePair asp) -> essentials::IdentifierConstPtr { return asp.first; });
        for (Pair<ID, State> asp : asps.getData()) {

            if (!oAgents.contains(asp.fst))
                oAgents.add(asp.fst);
        }
    }

    void getAgentsWorkingAndFinished(EntryPoint ep, ArrayList<ID> oAgents) {
        ArrayList<EntryPoint> eps = this.plan.getEntryPoints();
        int numEps = eps.size();

        for (int i = 0; i < numEps; ++i) {

            if (ep == eps.get(i)) {
                oAgents.ensureCapacity(this.assignmentData.get(i).size() + this.successData.getData().get(i).size());

                for (AgentStatePairs asps : this.assignmentData) {

                    for (Pair<ID, State> asp : asps.getData()) {

                        if (!oAgents.contains(asp.fst))
                            oAgents.add(asp.fst);
                    }
                }
//                std::copy(_successData.getRaw()[i].begin(), _successData.getRaw()[i].end(), std::back_inserter(o_agents));
//                CommonUtils.copy(this.successData.getData().get(i), oAgents);
                oAgents.addAll(this.successData.getData().get(i));
                return;
            }
        }
    }

    public void getAgentsInState(State s, ArrayList<ID> oAgents) {
        AgentStatePairs agentStatePairs = this.assignmentData.get(s.getEntryPoint().getIndex());

        for (Pair<ID, State> asp : agentStatePairs.getData()) {

            if (asp.snd == s) {
                oAgents.add(asp.fst);
            }
        }
    }

    public AssignmentView getAgentsWorking(EntryPoint ep) {
//        return this.getAgentStates(ep.getIndex());
        return new AssignmentView(this, ep.getIndex());
    }

    AssignmentView getAgentsWorking(int idx) {
//        return this.getAgentStates(idx);
        return new AssignmentView(this, idx);
    }

    AssignmentView getAgentsWorking(long epId) {
        EntryPoint ep = this.plan.getEntryPointByID(epId);
//        return ep != null ? this.getAgentStates(ep.getIndex()) : new AgentStatePairs();
        return ep != null ? new AssignmentView(this, ep.getIndex()) : new AssignmentView();
    }

    AssignmentSuccessView getAgentsWorkingAndFinished(EntryPoint ep) {
        return new AssignmentSuccessView(this, ep.getIndex());
    }

    public AllAgentsView getAllAgents() {
        return new AllAgentsView(this);
    }

    public AgentsInStateView getAgentsInState(State s) {
        return new AgentsInStateView(this, s);
    }

    AgentsInStateView getAgentsInState(long sid) {
        State s = this.plan.getStateByID(sid);
        return s != null ? new AgentsInStateView(this, s) : new AgentsInStateView();
    }

    void clear() {
        this.successData.clear();

        for (AgentStatePairs asp : this.assignmentData) {
            asp.clear();
        }
    }

    public boolean updateAgent(ID agent, EntryPoint e) {
        return updateAgent(agent, e, null);
    }

    boolean updateAgent(ID agent, EntryPoint e, State s) {
        boolean found = false;
        boolean inserted = false;
        int i = 0;
        assert (s == null || s.getEntryPoint() == e);

        for (AgentStatePairs asps : this.assignmentData) {
            boolean isTargetEp = e == this.plan.getEntryPoints().get(i);

            if (isTargetEp) {

                for (Pair<ID, State> asp : asps.getData()) {

                    if (asp.fst == agent) {
                        // assume a null state signals no change

                        if (s == null || asp.snd == s) {
                            return false;
                        } else {
                            asp.snd = s;
                            return true;
                        }
                    }
                }
                asps.add(agent, s != null ? s : e.getState());
                inserted = true;
            } else if (!found) {
                for (int j = 0; j < asps.size(); j++) {
                    if (asps.getData().get(j).fst == agent) {
                        found = true;
                        asps.removeAt(j);
                        break;
                    }
                }
            }
            if (found && inserted) {
                return true;
            }
            ++i;
        }
        return inserted;
    }

    void moveAllFromTo(EntryPoint scope, State from, State to) {
        assert (from.getEntryPoint() == scope);
        assert (to.getEntryPoint() == scope);

        for (int i = 0; i < this.assignmentData.size(); i++) {

            if (scope == this.plan.getEntryPoints().get(i)) {

                for (Pair<ID, State> asp : this.assignmentData.get(i).getData()) {

                    if (asp.snd == from) {
                        asp.snd = to;
                    }
                }
            }
        }
    }

    void setAllToInitialState(ArrayList<ID> agents, EntryPoint ep) {
        for (int i = 0; i < this.assignmentData.size(); i++) {
            boolean isTargetEp = ep.getIndex() == i;

            if (isTargetEp) {
                State s = ep.getState();

                for (ID id : agents) {
                    Pair<ID, State> it = null;

                    for (Pair<ID, State> asp : this.assignmentData.get(i).getData()) {

                        if (asp.fst == id) {
                            it = asp;
                        }
                    }
                    if (it == null) {
                        this.assignmentData.get(i).getData().add(new Pair<ID, State>(id, s));
                    } else {
                        it.snd = s;
                    }
                }
            } else {
                this.assignmentData.get(i).removeAllIn(agents);
            }
        }
    }

    Assignment adaptTaskChangesFrom(Assignment as) {

        if (as.getPlan() != this.getPlan()) {
//        *this = as;
            CommonUtils.aboutImplIncomplete(" use the return object");
            return as;
        }
        int epCount = this.assignmentData.size();

        for (int i = 0; i < epCount; ++i) {
//            AgentStatePairs n = as.this.assignmentData[i];
            AgentStatePairs n = as.assignmentData.get(i);

            for (Pair<ID, State> asp : n.getData()) {
                State s = this.assignmentData.get(i).getStateOfAgent(asp.fst);
                if (s != null) {
                    asp.snd = s;
                }
            }
            CommonUtils.move(n.getData(), this.assignmentData.get(i).getData());
        }
        return as;
    }

    boolean removeAllIn(ArrayList<Long> limit, State watchState) {
        boolean ret = false;
        int epCount = this.assignmentData.size();

        for (int i = 0; i < epCount; ++i) {

            for (int j = this.assignmentData.get(i).size() - 1; j >= 0; --j) {
                ID id = this.assignmentData.get(i).getData().get(j).fst;

                if (limit.contains(id)) {
                    ret = ret || this.assignmentData.get(i).getData().get(j).snd == watchState;
                    this.assignmentData.get(i).removeAt(j);
                }
            }
        }
        return ret;
    }

    boolean removeAllNotIn(ArrayList<ID> limit, State watchState) {
        boolean ret = false;
        int epCount = this.assignmentData.size();

        for (int i = 0; i < epCount; ++i) {

            for (int j = this.assignmentData.get(i).size() - 1; j >= 0; --j) {
                ID id = this.assignmentData.get(i).getData().get(j).fst;

                if (!limit.contains(id)) {
                    ret = ret || this.assignmentData.get(i).getData().get(j).snd == watchState;
                    this.assignmentData.get(i).removeAt(j);
                }
            }
        }
        return ret;
    }

    void removeAgent(ID agent) {
        int epCount = this.assignmentData.size();

        for (int i = 0; i < epCount; ++i) {

            Pair<ID, State> it = null;
            for (Pair<ID, State> asp : this.assignmentData.get(i).getData()) {

                if (agent == asp.fst)
                    it = asp;
            }
//            auto it = std::find_if(this.assignmentData[i].begin(), this.assignmentData[i].end(), [agent](AgentStatePair asp) { return agent == asp.first; });
            if (it != null) {
                this.assignmentData.get(i).getData().remove(it);
                return;
            }
        }
        return;
    }

    public void fillPartial(PartialAssignment pa) {
        int epCount = this.assignmentData.size();
        ArrayList<ID> allAgents = pa.getProblem().getAgents();

        for (int i = 0; i < epCount; ++i) {
            for (Pair<ID, State> asp : this.assignmentData.get(i).getData()) {

//                if (allAgents.contains(asp.fst)) {

                for (int index = 0; index < allAgents.size(); index++) {

                    if (allAgents.get(index) == asp.fst) {
                        int agentIdx = index;
                        pa.assignUnassignedAgent(agentIdx, i);
                        break;
                    }
                }
//                }
            }
        }
    }

    // -- getter setter --
    public Plan getPlan() {
        return this.plan;
    }

    int size() {
        int sum1 = this.assignmentData.stream().mapToInt(asps -> asps.size()).sum();
        int sum2 = this.assignmentData.stream().mapToInt(AgentStatePairs::size).sum();
        int sum3 = this.assignmentData.stream().collect(Collectors.summingInt(AgentStatePairs::size));
        int sum4 = 0;
        for (AgentStatePairs asps : this.assignmentData)
            sum4 += asps.size();

        if (sum4 == sum1 || sum4 == sum2 || sum4 == sum3)
            CommonUtils.aboutImplIncomplete(" Lambda works -> refactor code");
        return sum4;
    }

    public int getEntryPointCount() {
        return this.assignmentData.size();
    }

    public EntryPoint getEntryPoint(int idx) {
        return this.plan.getEntryPoints().get(idx);
    }

    //    private AgentStatePairs getAgentStates(int index) {  return this.assignmentData.get(index);   }
    public AgentStatePairs getAgentStates(int idx) {
        return this.assignmentData.get(idx);
    }

    public ArrayList<ID> getSuccessData(int idx) {
        return this.successData.getAgentsByIndex(idx);
    }

    ArrayList<ID> getSuccessData(EntryPoint ep) {
        return this.successData.getAgentsByIndex(ep.getIndex());
    }

    SuccessCollection getSuccessData() {
        return this.successData;
    }

    double getLastUtilityValue() {
        return this.lastUtility;
    }

    void addAgent(ID agent, EntryPoint e, State s) {
        this.assignmentData.get(e.getIndex()).add(agent, s);
    }

    void setState(ID agent, State s, EntryPoint hint) {
        this.assignmentData.get(hint.getIndex()).setStateOfAgent(agent, s);
    }

    void removeAgentFrom(ID agent, EntryPoint ep) {
        this.assignmentData.get(ep.getIndex()).remove(agent);
    }

    void removeAllFrom(ArrayList<ID> agents, EntryPoint ep) {
        this.assignmentData.get(ep.getIndex()).removeAllIn(agents);
    }

    @Override
    public String toString() {
        String out = " ";
        ArrayList<EntryPoint> eps = this.plan.getEntryPoints();
        int numEps = eps.size();
        assert (numEps == this.assignmentData.size());

        for (int i = 0; i < numEps; ++i) {
            out += "EP: " + eps.get(i).getID() + " Task: " + eps.get(i).getTask().getName() + " AgentIDs: ";

            for (Pair<ID, State> rsp : this.assignmentData.get(i).getData()) {
                out += rsp.fst + "(" + rsp.snd.getName() + ") ";
            }
            out += ", ";
        }
        out +=  this.successData + "\n";
        return out;
    }

    // -- views --
//    public class AssignmentSuccessView {
//        private final AgentStatePairs agentStates;
//        private final ArrayList<Long> successData;
//        private boolean inSuccess;
//        private Assignment assignment;
//        private int index;
//
//        public AssignmentSuccessView(Assignment assignment, int index) {
//            this.assignment = assignment;
//            this.index = index;
//            agentStates = this.assignment.getAgentStates(index);
//            successData = this.assignment.getSuccessData(index);
//
////            if (!inSuccess && agentStates != null && this.index >= agentStates.size()) {
////                this.inSuccess = true;
////                this.index = 0;
////            }
////        return AssignmentSuccessIterator(0, false, &_assignment->getAgentStates(_epIdx), &_assignment->getSuccessData(_epIdx));
////        return AssignmentSuccessIterator(_assignment->getSuccessData(_epIdx).size(), true, &_assignment->getAgentStates(_epIdx), &_assignment->getSuccessData(_epIdx));
//        }
//    }

//    public class AssignmentView {
//        protected Assignment assignment;
//        private int index;
//
//        public AssignmentView() {}
//
//        public AssignmentView(Assignment assignment, int index) {
//            this.assignment = assignment;
//            this.index = index;
//        }
//
//        public ArrayList<Long> get() {
//            // TODO : use Lambda
//            ArrayList<Long> agents = new ArrayList<>();
//            AgentStatePairs agentStates = this.assignment.getAgentStates(index);
//
//            for (Pair<Long, State> data : agentStates.getData()) {
//                agents.add(data.fst);
//            }
//            return agents;
//        }
//    }

//    public class AllAgentsView {
//        private Assignment assignment;
//
//        public AllAgentsView(Assignment assignment) {
//            this.assignment = assignment;
//        }
//
//        public ArrayList<Long> get() {
//            //TODO : use Lambda
//            ArrayList<Long> agents = new ArrayList<>();
//
//            for (int i = 0; i < this.assignment.getEntryPointCount(); i++) {
//                AgentStatePairs agentStates = this.assignment.getAgentStates(i);
//
//                for (Pair<Long, State> data : agentStates.getData()){
//                    agents.add(data.fst);
//                }
//            }
//            return agents;
////            this.assignment.getAgentStates(Idx).getData().get(agentIdx).fst;
//        }
//
//        public int size() {
//            if (this.assignment == null) {
//                return 0;
//            }
//            int ret = 0;
//            for (int i = 0; i < this.assignment.getEntryPointCount(); i++) {
//                ret += this.assignment.getAgentStates(i).size();
//            }
//            return ret;
//        }
//    }

//    public class AgentsInStateView {
//        private Assignment assignment;
//        private State state;
//
//        public AgentsInStateView() { }
//
//        public AgentsInStateView(Assignment assignment, State state) {
//            this.assignment = assignment;
//            this.state = state;
//        }
//
//        public ArrayList<Long> get() {
//            // TODO: use Lambda
//            ArrayList<Long> agents = new ArrayList<>();
//
//            AgentStatePairs agentStates = this.assignment.getAgentStates(this.state.getEntryPoint().getIndex());
//            for (Pair<Long, State> data : agentStates.getData()) {
//                agents.add(data.fst);
//            }
//            return agents;
//        }
//    }
//    public Assignment(Plan p) {
//        this.plan = p;
//        this.max = 0.0;
//        this.min = 0.0;
//
//        this.epAgentsMapping = new AssignmentCollection(this.plan.getEntryPoints().size());
//
//        // sort the entrypoints of the given plan
//        ArrayList<EntryPoint> sortedEpList = new ArrayList<>();
//
//        for (EntryPoint pair : plan.getEntryPoints().values()) {
//            sortedEpList.add(pair);
//        }
//        sortedEpList.sort(EntryPoint::compareTo);
//
//        // add the sorted entrypoints into the assignmentcollection
////        short i = 0;
//
//        for (int i = 0; i < sortedEpList.size(); i++) { //EntryPoint ep : sortedEpList) {
//            this.epAgentsMapping.setEp(i, sortedEpList.get(i));
//        }
//        this.epAgentsMapping.sortEps();
//
//        this.agentStateMapping = new StateCollection(this.epAgentsMapping);
//        this.epSucMapping = new SuccessCollection(p);
//    }
//
//    public Assignment(Plan p, AllocationAuthorityInfo aai) {
//        this.plan = p;
//        this.max = 1;
//        this.min = 1;
//
//        this.epAgentsMapping = new AssignmentCollection(p.getEntryPoints().size());
//
//        Vector<Long> curentAgents;
//        short i = 0;
//
//        for (EntryPoint epPair : p.getEntryPoints().values()) {
//
//            // set the entrypoint
//            if (!this.epAgentsMapping.setEp(i, epPair)) {
//                System.err.println(  "Ass: AssignmentCollection Index out of entrypoints bounds!" );
//                try {
//                    throw new Exception();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            curentAgents = new  Vector<>();
//            for (EntryPointAgents epAgents : aai.entryPointAgents) {
//
//                // find the right entrypoint
//                if (epAgents.entrypoint == epPair.getID()) {
//
//                    // copy agents
//                    for (long agent : epAgents.agents) {
//                        curentAgents.add(agent);
//                    }
//
//                    // set the agents
//                    if (!this.epAgentsMapping.setAgents(i, curentAgents)) {
//                        System.err.println("Ass: AssignmentCollection Index out of agents bounds!" );
//                        try {
//                            throw new Exception();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    break;
//                }
//            }
//            i++;
//        }
//
//        this.epSucMapping = new SuccessCollection(p);
//        this.agentStateMapping = new StateCollection(this.epAgentsMapping);
//    }
//
//    public Assignment(PartialAssignment pa) {
//        this.max = pa.getMax();
//        this.min = max;
//        this.plan = pa.getPlan();
//
//        AssignmentCollection assCol = pa.getEpAgentsMapping();
//
//        if (AssignmentCollection.allowIdling) {
//            this.epAgentsMapping = new AssignmentCollection(assCol.getSize() - 1);
//        }
//        else {
//            this.epAgentsMapping = new AssignmentCollection(assCol.getSize());
//        }
//
//        Vector<Long> curAgents;
//        for (short i = 0; i < this.epAgentsMapping.getSize(); i++) {
//            // set the entrypoint
//            if (!this.epAgentsMapping.setEp(i, assCol.getEntryPoint(i))) {
//                CommonUtils.aboutError("Ass: AssignmentCollection Index out of entrypoints bounds!");
//            }
//
//            // copy agents
//            Vector<Long> agents = assCol.getAgents(i);
//            curAgents = new Vector<Long>();
//
//            for (long rob : agents) {
//                curAgents.add(rob);
//            }
//
//            // set the agents
//            if (!this.epAgentsMapping.setAgents(i, curAgents)) {
//                CommonUtils.aboutError("Ass: AssignmentCollection Index out of agents bounds!");
//            }
//        }
//        this.agentStateMapping = new StateCollection(this.epAgentsMapping);
//        this.epSucMapping = pa.getEpSuccessMapping();
//
//    }
//
//    public void moveAgents(State from, State to) {
//
//        Set<Long> movingAgents = this.agentStateMapping.getAgentsInState(from);
//        if (to == null)
//        {
//            System.out.println("A: MoveAgents is given a State which is NULL!");
//        }
//        for (long r : movingAgents)
//        {
//            this.agentStateMapping.setState(r, to);
//        }
//    }
//
//    public SuccessCollection getEpSuccessMapping() {
//        return epSucMapping;
//    }
//
//    public boolean removeAgent(long agentID) {
//
//        this.agentStateMapping.removeAgent(agentID);
//        Vector<Long> curentAgents;
//        for (int i = 0; i < this.epAgentsMapping.getSize(); i++)
//        {
//            curentAgents = this.epAgentsMapping.getAgents(i);
//
//            //TODO: why is curentAgents size zero?
//            if(curentAgents.isEmpty())
//                return false;
//
//            int index = CommonUtils.findIndex(curentAgents, 0, curentAgents.size(), agentID);
//
//
//            if (index > -1)
//            {
//                curentAgents.remove(index);
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public boolean removeAgent(long agent, EntryPoint ep) {
//
//        if (ep == null) {
//            return false;
//        }
//        this.agentStateMapping.removeAgent(agent);
//
//        if (this.epAgentsMapping.getAgentsByEp(ep).contains(agent)) {
//            this.epAgentsMapping.getAgentsByEp(ep).remove(agent);
//            return true;
//        }
//		else {
//            return false;
//        }
//    }
//
//    public void addAgent(long agentID, EntryPoint entryPoint, State state) {
//
//        if (entryPoint == null) {
//            return;
//        }
//        this.agentStateMapping.setState(agentID, state);
//
//        // TODO: fix -> this.epAgentsMapping.agents teamObserver LinkedHashSet
//        if (!this.epAgentsMapping.getAgentsByEp(entryPoint).contains(agentID)) {
//            this.epAgentsMapping.getAgentsByEp(entryPoint).add(agentID);
//        }
//        return;
//    }
//
//    public void clear() {
//        this.agentStateMapping.clear();
//        this.epAgentsMapping.clear();
//        this.epSucMapping.clear();
//    }
//
//    public void setAllToInitialState(ArrayList<Long> agents, EntryPoint defep) {
//
//        for (long r : agents) {
//            this.epAgentsMapping.addAgentsByEp(r, defep);
//        }
//
//        for (long r : agents) {
//            this.agentStateMapping.setState(r, defep.getState());
//        }
//    }
//
//    public StateCollection getAgentStateMapping() {
//        return agentStateMapping;
//    }
//
//    public Plan getPlan() {
//        return plan;
//    }
//
//    public EntryPoint getEntryPointOfAgent(long agent) {
//
//        for (int i = 0; i < this.epAgentsMapping.getSize(); i++) {
//            Long iter = CommonUtils.find(this.epAgentsMapping.getAgents(i), 0, this.epAgentsMapping.getAgents(i).size(), Long.valueOf(agent));
////            if (iter != this.epAgentsMapping.getAgents(i).get(this.epAgentsMapping.getAgents(i).size()-1))
//
//            if (iter != null) {
//                return this.epAgentsMapping.getEntryPoint(i);
//            }
//        }
//        return null;
//    }

//    public Vector<Long> getAgentsWorking(long entryPointID) {
//        return this.getEpAgentsMapping().getAgentsByID(entryPointID);
//    }
//
////    public Vector<Long> getRobotsWorking(long epid) {
////        return this.getEpAgentsMapping().getAgentsByID(epid);
////    }
//
//    public Vector<Long> getAgentsWorking(EntryPoint ep) {
//        return this.getEpAgentsMapping().getAgentsByEp(ep);
//    }

//    Vector<Long>  getAgentsWorking(EntryPoint ep) {
//        return this.getEpAgentsMapping().getAgentsByEp(ep);
//    }

//    public int totalRobotCount() {
//        int c = 0;
//
//        for (int i = 0; i < this.epAgentsMapping.getSize(); i++) {
//            c += this.epAgentsMapping.getAgents(i).size();
//        }
//        return this.getNumUnAssignedRobots() + c;
//    }
//
//    public AssignmentCollection getEpAgentsMapping() {
//        return epAgentsMapping;
//    }
//
//    public boolean updateAgent(long agentID, EntryPoint ep) {
//        boolean ret = false;
//        for (int i = 0; i < this.epAgentsMapping.getSize(); i++)
//        {
//            if (this.epAgentsMapping.getEntryPoint(i) == ep)
//            {
//                if (CommonUtils.find(this.epAgentsMapping.getAgents(i),0, this.epAgentsMapping.getAgents(i).size()-1,
//                    agentID) != this.epAgentsMapping.getAgents(i).lastElement())
//                {
//                    return false;
//                }
//				else
//                {
//                    this.epAgentsMapping.getAgents(i).add(agentID);
//                    ret = true;
//                }
//            }
//			else
//            {
//                Long iter = CommonUtils.find(this.epAgentsMapping.getAgents(i), 0, this.epAgentsMapping.getAgents(i).size() - 1, agentID);
//                if (iter != this.epAgentsMapping.getAgents(i).lastElement())
//                {
//                    this.epAgentsMapping.getAgents(i).remove(iter);
//                    ret = true;
//                }
//            }
//        }
//        if (ret)
//        {
//            this.agentStateMapping.setState(agentID, ep.getState());
//        }
//        return ret;
//    }
//
//    public Vector<Long> getAllAgents() {
//        Vector<Long> ret = new Vector<>();
//        for (int i = 0; i < this.epAgentsMapping.getSize(); i++)
//        {
//            for (int j = 0; j < this.epAgentsMapping.getAgents(i).size(); j++)
//            {
//                ret.add(this.epAgentsMapping.getAgents(i).get(j));
//            }
//        }
//        return ret;
//    }
//
//    public boolean isValid() {
//        ArrayList<Long>[] success = this.epSucMapping.getAgents();
//
//        for (int i = 0; i < this.epAgentsMapping.getSize(); i++)
//        {
//            int c = this.epAgentsMapping.getAgents(i).size() + success[i].size();
//            if (c > this.epAgentsMapping.getEntryPoint(i).getMaxCardinality()
//                || c < this.epAgentsMapping.getEntryPoint(i).getMinCardinality())
//            {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    public Vector<Long> getUnassignedAgents() {
//        return unassignedAgents;
//    }
//
//
//    public double getMax() {
//        return max;
//    }
//
//    public boolean isSuccessfull() {
//        for (int i = 0; i < this.epSucMapping.getCount(); i++)
//        {
//            if (this.epSucMapping.getEntryPoints()[i].getSuccessRequired())
//            {
//                if (!(this.epSucMapping.getAgents()[i].size() > 0
//                    && this.epSucMapping.getAgents()[i].size()
//                    >= this.epSucMapping.getEntryPoints()[i].getMinCardinality()))
//                {
//                    return false;
//                }
//            }
//
//        }
//        return true;
//    }
//
//    @Override
//    public int getEntryPointCount() {
//        return this.epAgentsMapping.getSize();
//    }
//
//    @Override
//    public ArrayList<Long> getAgentsWorkingAndFinished(EntryPoint ep) {
//        ArrayList<Long> ret = new ArrayList<>();
//        Vector<Long> agents = this.epAgentsMapping.getAgentsByEp(ep);
//
//        if (agents != null) {
//
//            for (int i = 0; i < agents.size(); i++) {
//                ret.add(agents.get(i));
//            }
//        }
//        ArrayList<Long> succAgents = this.epSucMapping.getAgents(ep);
//
//        if (succAgents != null) {
//
//            for (int i = 0; i < succAgents.size(); i++) {
//                ret.add(succAgents.get(i));
//            }
//        }
//        return ret;
//    }
//
//    @Override
//    public ArrayList<Long> getUniqueAgentsWorkingAndFinished(EntryPoint ep) {
//        ArrayList<Long> ret = new ArrayList<>();
//
//        if (this.plan.getEntryPoints().containsKey(ep.getID()))
//        {
//            Vector<Long> agents = this.epAgentsMapping.getAgentsByEp(ep);
//
//            for (int i = 0; i < agents.size(); i++) {
//                ret.add(agents.get(i));
//            }
//
//            for (long r : this.epSucMapping.getAgents(ep)) {
//
//                if (CommonUtils.find(ret, 0, ret.size()-1, r) == ret.get(ret.size()-1)) {
//                    ret.add(r);
//                }
//            }
//        }
//        return ret;
//    }
//
//    public boolean updateAgent(long agent, EntryPoint ep, State s) {
//        this.agentStateMapping.setState(agent, s);
//        boolean ret = false;
//
//        for (int i = 0; i < this.epAgentsMapping.getSize(); i++) {
//
//            if (this.epAgentsMapping.getEntryPoint(i) == ep) {
//
//                if (this.epAgentsMapping.getAgents(i).contains(agent)) {
//                    return false;
//                }
//                else {
//                    this.epAgentsMapping.getAgents(i).add(agent);
//                    ret = true;
//                }
//            }
//            else
//            {
//                if ( this.epAgentsMapping.getAgents(i).contains(agent))
//                {
//                    this.epAgentsMapping.getAgents(i).remove(agent);
//                    ret = true;
//                }
//            }
//        }
//        return ret;
//    }

//    @Override
//    public void setMin(double min) {
//        CommonUtils.aboutNoImpl();
//    }
//
//    @Override
//    public void setMax(double max) {
//        CommonUtils.aboutNoImpl();
//    }
//
//    @Override
//    public String toString() {
//        return "ASS\n" + this.epAgentsMapping.toString();
//    }

}
