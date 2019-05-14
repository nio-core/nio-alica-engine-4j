package de.uniks.vs.jalica.behaviours;

import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.unknown.AbstractPlan;
import de.uniks.vs.jalica.unknown.AlicaElement;

import java.util.ArrayList;

/**
 * Created by alex on 13.07.17.
 */
public class Behaviour extends AbstractPlan {
    ArrayList<BehaviourConfiguration> configurations = new ArrayList<>();

    /**
     * The actual implementation of this behaviour, a subclass of BasicBehaviour
     */
    BasicBehaviour implementation;
    String fileName;

    public Behaviour() {
        super(null);
    }

    public Behaviour(String name) {
        super(null);
        this.name = name;
    }

    public Behaviour(AlicaEngine ae) {
        super(ae);
    }

    public Behaviour(AlicaEngine ae, String name) {
        super(ae);
        this.name = name;
    }

    public BasicBehaviour getImplementation() {
        return implementation;
    }

    public void setImplementation(BasicBehaviour implementation) {
        this.implementation = implementation;
    }

    public String getFileName() {

        if (this.getFileName().isEmpty())
        {
            String result = name + ".beh";
            return result;
        }
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ArrayList<BehaviourConfiguration> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(ArrayList<BehaviourConfiguration> configurations) {
        this.configurations = configurations;
    }

    public String toString()
    {
        String ss = "";
        ss += "#Behaviour: " + this.getName()+ "\n";
        ss += "\t Configurations: " + this.getConfigurations().size()+ "\n";

        for(BehaviourConfiguration bc : this.getConfigurations()) {
            ss += "\t" + bc.getName() + " " + bc.getID()+ "\n";
        }
        ss += "#EndBehaviour\n";
        return ss;
    }
}
