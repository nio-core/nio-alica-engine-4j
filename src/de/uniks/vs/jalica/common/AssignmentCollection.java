package de.uniks.vs.jalica.common;

import de.uniks.vs.jalica.unknown.EntryPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

/**
 * Created by alex on 13.07.17.
 */
public class AssignmentCollection {

    public static short maxEpsCount;
    public static Boolean allowIdling;

    private int numEps;
    private Vector<Vector<Integer>> agents;
    private ArrayList<EntryPoint> entryPoints;

    public AssignmentCollection(int size) {
        this.numEps = size;
        this.entryPoints = new ArrayList<EntryPoint>(size);
        this.agents = new Vector<>();

        for (short i = 0; i < size; i++) {
            this.agents.add(i, new Vector<Integer>());
        }
    }

    public int getSize() {
        return this.numEps;
    }

    public Vector<Integer> getAgents(int index) {
        if (index < this.numEps)
        {
            return this.agents.get(index);
        }
		else
        {
            return null;
        }
    }

    public Vector<Integer> getAgentsByEp(EntryPoint ep) {
        for (int i = 0; i < this.numEps; i++)
        {
            if (this.entryPoints.get(i) == ep)
            {
                return this.agents.get(i);
            }
        }
        return null;
    }

    public void clear() {
        for (int i = 0; i < this.numEps; i++)
        {
            this.agents.get(i).clear();
        }
    }

    public EntryPoint getEp(int index) {

        if (index < this.numEps) {
            return this.entryPoints.get(index);
        }
		else {
            return null;
        }
    }

    public boolean setAgents(short index, Vector<Integer> agents) {
        if (index < this.numEps) {
            this.agents.set(index,agents);
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
            this.entryPoints.add(index, ep);
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

        for (short i = 0; i < this.numEps; i++)  {
            sortedEpVec.add(this.entryPoints.get(i));
        }
//        CommonUtils.stable_sort(sortedEpVec.begin(), sortedEpVec.end(), EntryPoint.compareTo());
        Collections.sort( sortedEpVec );

        for (short i = 0; i < this.numEps; i++) {
            this.entryPoints.add(i, sortedEpVec.get(i));
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

    public ArrayList<EntryPoint> getEntryPoints() {
        return entryPoints;
    }

    @Override
    public String toString() {
        String string = "";

        for (int i = 0; i < this.numEps; i++) {

            if (this.entryPoints.get(i) != null){
                string += this.entryPoints.get(i).getId() + " : ";

                for (int robot : this.agents.get(i)) {
                    string += robot + ", ";
                }
                string += "\n";
            }
        }
        return string;
    }
}
