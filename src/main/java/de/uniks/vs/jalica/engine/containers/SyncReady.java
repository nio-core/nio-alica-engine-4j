package de.uniks.vs.jalica.engine.containers;

import de.uniks.vs.jalica.engine.containers.messages.Tuple;
import de.uniks.vs.jalica.engine.idmanagement.ID;

/**
 * Created by alex on 10.11.17.
 * updated 23.6.19
 */
public class SyncReady {
    public ID senderID;
    public long synchronisationID;

    public SyncReady() {
        this.senderID = null;
        this.synchronisationID = 0;
    }

    public SyncReady(Tuple s) {
            this.senderID = (ID) s.get()[0];
            this.synchronisationID = (long) s.get()[1];
    }

    Tuple toStandard() { return new Tuple(senderID, synchronisationID); }
}
