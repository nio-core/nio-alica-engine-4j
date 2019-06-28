package de.uniks.vs.jalica.engine.syncmodule;

import de.uniks.vs.jalica.engine.common.SyncData;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by alex on 11.11.17.
 */
public class SyncRow {

    SyncData syncData;
    boolean haveData;
    // this vector always has to be sorted
    ArrayList<Long> receivedBy;

    public SyncRow() {
        this.haveData = false;
        this.receivedBy = new ArrayList<>();
    }

    SyncRow(SyncData sd) {
        this.syncData = sd;
        this.haveData = true;
        this.receivedBy = new ArrayList<>();
    }

    ArrayList<Long> getReceivedBy() {
        this.receivedBy.sort(Comparator.naturalOrder());
        return this.receivedBy;
    }

    void setReceivedBy(ArrayList<Long> receivedBy) {
        this.receivedBy = receivedBy;
    }

    void setSyncData(SyncData syncData) {
        this.syncData = syncData;
    }

    SyncData getSyncData() {
        return this.syncData;
    }

    boolean hasData() {
        return this.haveData;
    }

    void invalidate() {
        this.haveData = false;
    }


    @Override
    public java.lang.String toString() { // TODO: fix this method (doesnt produce a string, but write to cout)
        String s = "SyncRow" + "\n";
        s += "ReceivedBy: " + "\n";
        for (long i : this.receivedBy) {
            s += i + " ";
        }
        s += "\n";
        s += this.syncData.toString();
        return s;
    }
}
