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
        Long val = -1l;

        do {
            final UUID uuid = UUID.randomUUID();
            final ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
            buffer.putLong(uuid.getLeastSignificantBits());
            buffer.putLong(uuid.getMostSignificantBits());
            final BigInteger bi = new BigInteger(buffer.array());
            val = bi.longValue();
        }
        while (val < 0 || uids.contains(val));
        uids.add(val);
        return val;
    }
}
