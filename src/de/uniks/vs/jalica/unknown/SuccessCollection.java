package de.uniks.vs.jalica.unknown;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by alex on 18.07.17.
 */
public class SuccessCollection {

    EntryPoint entryPoints;
    Vector<ArrayList<Integer>> robots;
    int count = 0;

    public ArrayList<Integer> getRobots(EntryPoint entryPoint) {
        for (int i = 0; i < this.count; i++) {
            if (this.getEntryPoints() == entryPoint) {
                return this.robots.get(i);
            }
        }
        return null;
    }

    public EntryPoint getEntryPoints() {
        return entryPoints;
    }

    public void clear() {
        for (int i = 0; i < this.count; i++)
        {
            this.robots.get(i).clear();
        }
    }
}
