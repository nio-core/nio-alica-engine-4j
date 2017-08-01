package de.uniks.vs.jalica.common;

import de.uniks.vs.jalica.unknown.CommonUtils;
import de.uniks.vs.jalica.unknown.EntryPoint;

import java.lang.reflect.Array;
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
    Vector<Vector<Integer>> robots;
    private ArrayList<EntryPoint> entryPoints;
    private int size;

    public AssignmentCollection(int size) {
        this.numEps = size;
        this.entryPoints = new ArrayList<EntryPoint>(size);
        this.robots = new Vector<>();
        for (short i = 0; i < size; i++)
        {
            this.robots.set(i, new Vector<Integer>());
        }
    }

    public int getSize() {
        return this.numEps;
    }

    public Vector<Integer> getRobots(int index) {
        if (index < this.numEps)
        {
            return this.robots.get(index);
        }
		else
        {
            return null;
        }
    }

    public Vector<Integer> getRobotsByEp(EntryPoint ep) {
        for (int i = 0; i < this.numEps; i++)
        {
            if (this.entryPoints.get(i) == ep)
            {
                return this.robots.get(i);
            }
        }
        return null;
    }

    public void clear() {
        for (int i = 0; i < this.numEps; i++)
        {
            this.robots.get(i).clear();
        }
    }

    public EntryPoint getEp(int index) {
        if (index < this.numEps)
        {
            return this.entryPoints.get(index);
        }
		else
        {
            return null;
        }
    }

    public boolean setRobots(short index, Vector<Integer> robots) {
        if (index < this.numEps) {
            this.robots.set(index,robots);
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
            this.entryPoints.set(index, ep);
            return true;
        }
		else
        {
            System.out.println( "AssCol: Index to HIGH!!!!!! ########################################");
            return false;
        }
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void sortEps() {
        //		cout << "<<<< Check Sort!!!!! " << endl;
//		for (short i = 0; i < this.numEps; i++)
//		{
//			cout << i << ": " << entryPoints[i].getTask().getId() << endl;
//		}

        // Stopfers sort style
        Vector<EntryPoint> sortedEpVec = new Vector<>();
        for (short i = 0; i < this.numEps; i++)
        {
            sortedEpVec.add(this.entryPoints.get(i));
        }
//        CommonUtils.stable_sort(sortedEpVec.begin(), sortedEpVec.end(), EntryPoint.compareTo());
        Collections.sort( sortedEpVec );

        for (short i = 0; i < this.numEps; i++)
        {
            this.entryPoints.add(i, sortedEpVec.get(i));
        }

        // Takers sort style
		/*
		 std::sort(std::begin(entryPoints), std::begin(entryPoints) + this.numEps, EpByTaskComparer::compareTo);
		 */

//		cout << "<<<<< Nachher!!!! " << endl;
//		for (short i = 0; i < this.numEps; i++)
//		{
//			cout << i << ": " << entryPoints[i].getTask().getId() << endl;
//		}
    }
}
