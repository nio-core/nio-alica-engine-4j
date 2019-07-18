package de.uniks.vs.jalica.engine.syncmodule;

import de.uniks.vs.jalica.engine.common.SyncData;
import de.uniks.vs.jalica.engine.idmanagement.ID;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by alex on 11.11.17.
 */
public class SyncRow {

    SyncData syncData;
    boolean haveData;
    // this vector always has to be sorted
    ArrayList<ID> receivedBy;

    public SyncRow() {
        this.haveData = false;
        this.receivedBy = new ArrayList<>();
    }

    SyncRow(SyncData sd) {
        this.syncData = sd;
        this.haveData = true;
        this.receivedBy = new ArrayList<>();
    }

    ArrayList<ID> getReceivedBy() {
        //TODO: check sorting
        this.receivedBy.sort(Comparator.comparing(value -> value.asLong()));
//        this.receivedBy.sort((o1, o2) -> (o1.asLong() < o2.asLong() ? -1 : 1));
//        this.receivedBy.sort(Comparator.naturalOrder());
        return this.receivedBy;
    }

    void setReceivedBy(ArrayList<ID> receivedBy) {
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
    public String toString() { // TODO: fix this method (doesnt produce a string, but write to cout)
        String s = "SyncRow" + "\n";
        s += "ReceivedBy: " + "\n";
        for (ID i : this.receivedBy) {
            s += i + " ";
        }
        s += "\n";
        s += this.syncData.toString();
        return s;
    }
}
