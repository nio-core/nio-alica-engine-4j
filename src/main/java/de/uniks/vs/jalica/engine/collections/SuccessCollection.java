package de.uniks.vs.jalica.engine.collections;

import de.uniks.vs.jalica.engine.model.EntryPoint;
import de.uniks.vs.jalica.engine.model.Plan;

import java.util.ArrayList;

/**
 * Created by alex on 18.07.17.
 * updated 23.6.19
 */
public class SuccessCollection {

    private Plan plan;
    private ArrayList<ArrayList<Long>> successData;

    // Type definitions --------------------
    // AgentGrp = ArrayList<Long>
    // EntryPointGrp = ArrayList<EntryPoint>
    // IdGrp = ArrayList<Long>
    // VariableGrp = ArrayList<Variable>
    // BehaviourParameterMap = HashMap<String, String>
    // TransitionGrp = ArrayList<Transition>
    // typedef std::tuple<Long, Long, Long, Long, Long, ArrayList<stdEntryPointRobot>> stdAllocationAuthorityInfo;
    // typedef std::tuple<Long, ArrayList<Long>> stdEntryPointRobot;


    public SuccessCollection() {
        this.plan = null;
        this.successData = new ArrayList<>();
    }

    public SuccessCollection(Plan plan) {
        this.plan = plan;
        this.successData = new ArrayList<>(plan.getEntryPoints().size());
    }



    public void setSuccess(long agentId, EntryPoint ep) {

        if (ep.getPlan() == this.plan) {
            this.successData.get(ep.getIndex()).add(agentId);
        }
    }

    public void clear() {
        for (ArrayList<Long> ag : this.successData) {
            ag.clear();
        }
    }

     public ArrayList<Long> getAgents(EntryPoint ep) {
        if (ep.getPlan() == this.plan) {
            return this.successData.get(ep.getIndex());
        }
        return null;
    }

    ArrayList<Long> getAgentsById(long id) {
        ArrayList<EntryPoint> eps = new ArrayList<>(this.plan.getEntryPoints());

        for (int i = 0; i < eps.size(); i++) {

            if (eps.get(i).getID() == id) {
                return successData.get(i);
        }
    }
        return null;
    }

    public ArrayList<Long> getAgentsByIndex(int index) {
        return this.successData.get(index);
    }

//    public ArrayList<Long> getAgentsByIndex(int index) {
//
//        if (index >= 0 && index < this.successData.size()) {
//            return this.successData.get(index);
//        }
//        return null;
//    }

    int getCount()  { return this.successData.size(); }

    ArrayList<EntryPoint> getEntryPoints() { return new ArrayList<>(this.plan.getEntryPoints()); }

    public ArrayList<ArrayList<Long>> getRaw() {
        return this.getData();
    }

    public ArrayList<ArrayList<Long>> getData() {
        return this.successData;
    }

    @Override
    public String toString() {
        String out = "";
        boolean haveAny = false;
        ArrayList<EntryPoint> eps = this.plan.getEntryPoints();
        for (int i = 0; i < eps.size(); ++i) {
            if (!this.successData.get(i).isEmpty()) {
                if (!haveAny) {
                    out += "Success:" + "\n";
                }
                haveAny = true;
                out += eps.get(i).getID() + " (" + eps.get(i).getTask().getName() + "): ";
                for (long r : this.successData.get(i)) {
                    out += r + " ";
                }
                out += "\n";
            }
        }
        if (!haveAny) {
            out += "No EntryPoint successful!";
        }
        return out;
    }
}
