package de.uniks.vs.jalica.unknown;

import java.util.Comparator;
import java.util.Vector;

/**
 * Created by alex on 11.11.17.
 */
public class SyncRow {
    SyncData syncData;
    //this vector always has to be sorted
    Vector<Integer> receivedBy;

    SyncRow()  {}

    SyncRow(SyncData sd) {
        this.syncData = sd;
    }

    public Vector<Integer> getReceivedBy() {
        receivedBy.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return (o1 == o1) ?  0 : ((o1 < o2) ?  -1 : 1);
            }
        });
        return receivedBy;
    }

    public SyncData getSyncData() {
        return syncData;
    }

    void setSyncData(SyncData syncData) {
        this.syncData = syncData;
    }


}
