package de.uniks.vs.jalica.behaviours;

import de.uniks.vs.jalica.engine.USummand;
import de.uniks.vs.jalica.engine.UtilityInterval;
import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.common.Pair;
import de.uniks.vs.jalica.engine.idmanagement.ID;
import de.uniks.vs.jalica.engine.planselection.IAssignment;

import java.util.ArrayList;
import java.util.Vector;

public class DummyTestSummand extends USummand {

    ID agentID;
    double sb;
    double angleBallOpp;
    double velAngle;

    public DummyTestSummand(double weight, String name, long id, Vector<Long> relevantEntryPointIds) {
        CommonUtils.aboutCallNotification();
        this.weight = weight;
        this.name = name;
        this.id = id;
        this.relevantEntryPointIds = relevantEntryPointIds;
        this.angleBallOpp = 0;
        this.velAngle = 0;
        this.agentID = null;
        this.sb = 0;
    }

    public void cacheEvalData() {
        CommonUtils.aboutNoImpl();
    }

    public UtilityInterval eval(IAssignment ass) {
        ui.setMin(0.0);
        ui.setMax(1.0);
        ArrayList<ID> relevantRobots = ass.getAgentsWorking(this.relevantEntryPoints.get(0));

        for (int i = 0; i < relevantRobots.size(); ++i) {
            int pos = 0;

            if (relevantRobots.get(i).asLong() == this.agentID.asLong()) {
                ui.setMin(0.5);
            }
			else {
                ui.setMin(0.0);
            }

        }

        if(this.relevantEntryPoints.size() > 1) {
            relevantRobots = ass.getAgentsWorking(this.relevantEntryPoints.get(1));

            for (int i = 0; i < relevantRobots.size(); ++i) {
                int pos = 0;

                if (relevantRobots.get(i).asLong() == this.agentID.asLong()) {
                    ui.setMin(ui.getMin());
                }
				else {
                    ui.setMin(ui.getMin() + 0.5);
                }
            }
        }
        ui.setMax(ui.getMin());
        return ui;
    }

    @Override
    public Pair<Vector<Double>, Double> differentiate(IAssignment newAss) {
        CommonUtils.aboutNoImpl();
        return null;
    }

    public void setAgentID(ID agentID) {
        this.agentID = agentID;
    }
}
