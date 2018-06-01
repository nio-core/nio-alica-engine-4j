package de.uniks.vs.jalica.common;

import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.unknown.Assignment;
import de.uniks.vs.jalica.unknown.CommonUtils;
import de.uniks.vs.jalica.unknown.EntryPoint;
import de.uniks.vs.jalica.unknown.IAssignment;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by alex on 31.07.17.
 */
public class USummand {

    UtilityInterval ui;
    Vector<Long> relevantEntryPointIds;

    private double weight;
    String name;
    long id;
    String info;
    Vector<EntryPoint> relevantEntryPoints;

    public UtilityInterval eval(IAssignment assignment) {
        CommonUtils.aboutNoImpl();
        return null;
    }

    public double getWeight() {
        return weight;
    }

    public void cacheEvalData() {CommonUtils.aboutNoImpl();}

    public void init(AlicaEngine ae) {
        // init relevant entrypoint vector
        this.relevantEntryPoints.setSize(this.relevantEntryPointIds.size());
        // find the right entrypoint for each id in relevant entrypoint id
        HashMap<Long, EntryPoint> elements = ae.getPlanRepository().getEntryPoints();
        EntryPoint curEp = null;

        for(int i = 0; i < this.relevantEntryPoints.size(); ++i) {
            EntryPoint iter = elements.get(this.relevantEntryPointIds.get(i));

            if(iter != null) {
                curEp = iter;
            }
            else {
                System.out.println("Could not find Entrypoint " + this.relevantEntryPointIds.get(i) + " Hint is: " + this.name );
                CommonUtils.aboutError("");
            }

            if(curEp != null) {
                this.relevantEntryPoints.add(i,curEp);
            }
        }
    }
}
