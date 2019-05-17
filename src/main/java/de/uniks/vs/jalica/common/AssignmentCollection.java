package de.uniks.vs.jalica.common;

import de.uniks.vs.jalica.unknown.CommonUtils;
import de.uniks.vs.jalica.unknown.EntryPoint;

import java.util.*;

/**
 * Created by alex on 13.07.17.
 */
public class AssignmentCollection {

    public static int maxEpsCount;
    public static Boolean allowIdling;

    private int numEps;
    private Vector<Long>[] agents;
    private EntryPoint[] entryPoints;

    public AssignmentCollection(int size) {
        this.numEps = size;
        this.entryPoints = new EntryPoint[size];
        this.agents = new Vector[size];

        for (short i = 0; i < size; i++) {
            this.agents[i] = new Vector();
        }
    }

    public int getSize() {
        return this.numEps;
    }

    public final Vector<Long> getAgents(int index) {

        if (index < this.numEps) {
            return this.agents[index];
        }
        return null;
    }

    public final Vector<Long> getAgentsByEp(long ep) {

        for (int i = 0; i < this.numEps; i++) {

            if (this.entryPoints[i].getID() == ep) {
                return this.agents[i];
            }
        }
        return null;
    }

    public final Vector<Long> getAgentsByEp(EntryPoint ep) {

        for (int i = 0; i < this.numEps; i++) {

            if (this.entryPoints[i] == ep) {
                return this.agents[i];
            }
        }
        return null;
    }
    public void addAgentsByEp(long agentID, EntryPoint ep) {

        for (int i = 0; i < this.numEps; i++) {

            if (this.entryPoints[i] == ep) {
                this.agents[i].add(agentID);
            }
        }
    }

    public void clear() {
        for (int i = 0; i < this.numEps; i++)
        {
            this.agents[i].clear();
        }
    }

    public EntryPoint getEp(int index) {

        if (index < this.numEps) {
            return this.entryPoints[index];
        }
		else {
            return null;
        }
    }

    public boolean setAgents(short index, Vector agents) {

        if (CommonUtils.AC_DEBUG_debug) System.out.println("AC: agent size " + agents.size() + "   " +agents);

        if (index < this.numEps) {
            this.agents[index] = agents;
            return true;
        }
		else
        {
            return false;
        }
    }

    public boolean setEp(int index, EntryPoint ep) {
        if (index < this.numEps)
        {
            this.entryPoints[index] = ep;
            return true;
        }
		else
        {
            System.out.println( "AssCol: Index to HIGH!!!!!! ########################################");
            return false;
        }
    }

    public void setSize(int size) {
        this.numEps = size;
    }

    public void sortEps() {
        //		cout << "<<<< Check Sort!!!!! " << endl;
//		for (short i = 0; i < this.numEps; i++)
//		{
//			cout << i << ": " << entryPoints[i].getTask().getID() << endl;
//		}

        // Stopfers sort style
        Vector<EntryPoint> sortedEpVec = new Vector<>();

//        int maxIndex = getMaxIndex();

        for (int i = 0; i < this.numEps; i++)  {
            sortedEpVec.add(this.entryPoints[i]);
        }
//        CommonUtils.stable_sort(sortedEpVec.begin(), sortedEpVec.end(), EntryPoint.compareTo());
        Collections.sort( sortedEpVec );

        for (int i = 0; i < this.numEps; i++) {
            this.entryPoints[i] = sortedEpVec.get(i);
        }

        // Takers sort style
		/*
		 std::sort(std::begin(entryPoints), std::begin(entryPoints) + this.numEps, EpByTaskComparer::compareTo);
		 */

//		cout << "<<<<< Nachher!!!! " << endl;
//		for (short i = 0; i < this.numEps; i++)
//		{
//			cout << i << ": " << entryPoints[i].getTask().getID() << endl;
//		}
    }

//    private int getMaxIndex() {
//        return (this.numEps > this.entryPoints.size()) ? this.entryPoints.size() : this.numEps;
//    }

    public EntryPoint[] getEntryPoints() {
        return entryPoints;
    }

    @Override
    public String toString() {
        String string = "";

        for (int i = 0; i < this.numEps; i++) {

            if (this.entryPoints[i] != null){
                string += this.entryPoints[i].getID() + " : ";

                for (long agent : this.agents[i]) {
                    string += agent + ", ";
                }
                string += "\n";
            }
        }
        return string;
    }

    public Vector<Long> getAgentsByID(long id) {

        for (int i = 0; i < this.numEps; i++) {

            if (this.entryPoints[i].getID() == id) {
                return this.agents[i];
            }
        }
        return null;
    }
}
