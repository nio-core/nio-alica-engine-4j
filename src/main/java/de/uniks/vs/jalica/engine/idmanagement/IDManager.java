package de.uniks.vs.jalica.engine.idmanagement;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class IDManager {

    static HashMap<ID, Object> uuids = new HashMap();
    static HashSet<Long> uids = new HashSet<>();

    public static ID generateUUID(Object object) {
        ID id = new ID();
            uuids.put(id, object);
            return id;
        }

    public static UUID stringToUUID(String string) {
        return UUID.fromString(string);
    }

    public static Long generateUniqueID() {
        Long uniqueID = -1l;

        do {
            final UUID uuid = UUID.randomUUID();
            final ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
            buffer.putLong(uuid.getLeastSignificantBits());
            buffer.putLong(uuid.getMostSignificantBits());
            final BigInteger bi = new BigInteger(buffer.array());
            uniqueID = bi.longValue();
        }
        while (uniqueID < 0 || uids.contains(uniqueID));
        uids.add(uniqueID);
        return uniqueID;
    }

    public static Long generateUniqueID(String string) {
        ID id = new ID();
        uuids.put(id, string);
        return id.asLong();
    }
}
