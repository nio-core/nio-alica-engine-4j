package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.model.EntryPoint;
import de.uniks.vs.jalica.engine.planselection.IAssignment;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by alex on 31.07.17.
 */
public abstract class USummand {

    //TODO: CPP remove logic from header, make differentiate method abstract

    protected UtilityInterval ui;
    protected Vector<Long> relevantEntryPointIds;
    protected Vector<EntryPoint> relevantEntryPoints;
    protected double weight;
    protected String name;
    protected long id;
    protected String info;

    public USummand() {
        ui = new UtilityInterval(0.0,0.0);
        relevantEntryPointIds = new Vector<>();
        relevantEntryPoints = new Vector<>();
        this.id = 0;
        this.weight = 0;
    }

    public abstract UtilityInterval eval(IAssignment assignment);

    public abstract Pair<Vector<Double>, Double> differentiate(IAssignment newAss);

    public abstract void cacheEvalData();

    public void init(AlicaEngine ae) {
        // init relevant entrypoint vector
        this.relevantEntryPoints.setSize(this.relevantEntryPointIds.size());
        // find the right entrypoint for each id in relevant entrypoint id
        HashMap<Long, EntryPoint> elements = ae.getPlanRepository().getEntryPoints();
        EntryPoint curEp = null;

        for(int i = 0; i < this.relevantEntryPoints.size(); i++) {
            EntryPoint iter = elements.get(this.relevantEntryPointIds.get(i));

            if(iter != null) {
                curEp = iter;
            }
            else {
                System.out.println("Could not find Entrypoint " + this.relevantEntryPointIds.get(i) + " Hint is: " + this.name );
                CommonUtils.aboutError("");
            }

            if(curEp != null) {
                this.relevantEntryPoints.set(i,curEp);
            }
        }
    }

    public String toString() {
        String ss = this.name + ": Weight " + this.weight + "EntryPoints: ";

        for(int i = 0; i < this.relevantEntryPointIds.size(); ++i) {
            ss += this.relevantEntryPointIds.get(i) + " ";
        }
        ss += "\n";
        return ss;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight( double weight) {
        this.weight = weight;
    }
}
