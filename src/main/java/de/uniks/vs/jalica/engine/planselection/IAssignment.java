package de.uniks.vs.jalica.engine.planselection;

import de.uniks.vs.jalica.engine.model.EntryPoint;
import de.uniks.vs.jalica.engine.model.State;
import de.uniks.vs.jalica.engine.planselection.views.PartialAssignmentSuccessView;
import de.uniks.vs.jalica.engine.planselection.views.PartialAssignmentView;
import de.uniks.vs.jalica.engine.planselection.views.UniquePartialAssignmentSuccessView;

import java.util.ArrayList;

/**
 * Created by alex on 17.07.17.
 * Updated 26.6.19
 */
public class IAssignment {

    private PartialAssignment impl;

    public IAssignment(PartialAssignment pa) {
        this.impl = pa;
    }

    public PartialAssignmentView getAgentsWorking(EntryPoint ep) {
        return new PartialAssignmentView(ep.getIndex(), this.impl);
    }

    PartialAssignmentView getAgentsWorking(long epid) {
        ArrayList<EntryPoint> eps = this.impl.getPlan().getEntryPoints();
        for (int i = 0; i < eps.size(); ++i) {
            if (eps.get(i).getID() == epid) {
                return new PartialAssignmentView(i, this.impl);
            }
        }
        // return safe value that does not exist. Magic number is used for debuggers.
        return new PartialAssignmentView(-42, this.impl);
    }

    public PartialAssignmentView getUnassignedAgents() {
        return new PartialAssignmentView(-1, this.impl);
    }
    public PartialAssignmentSuccessView getAgentsWorkingAndFinished(EntryPoint ep) {
        return new PartialAssignmentSuccessView(ep.getIndex(), this.impl);
    }

    PartialAssignmentSuccessView getAgentsWorkingAndFinished(long epid) {
        ArrayList<EntryPoint> eps = this.impl.getPlan().getEntryPoints();
        for (int i = 0; i < eps.size(); i++) {
            if (eps.get(i).getID() == epid) {
                return new PartialAssignmentSuccessView(i, this.impl);
            }
        }
        return new PartialAssignmentSuccessView(-42, this.impl);
    }

    public UniquePartialAssignmentSuccessView getUniqueAgentsWorkingAndFinished(EntryPoint ep) {
        return new UniquePartialAssignmentSuccessView(ep.getIndex(), this.impl);
    }


    int getTotalAgentCount()  { return this.impl.getTotalAgentCount(); }
    int getAssignedAgentCount()  { return this.impl.getAssignedAgentCount(); }
    int getUnAssignedAgentCount()  { return getTotalAgentCount() - getAssignedAgentCount(); }
    public int getEntryPointCount()  { return this.impl.getEntryPointCount(); }
    public EntryPoint getEntryPoint(int idx)  { return this.impl.getPlan().getEntryPoints().get(idx); }







}
