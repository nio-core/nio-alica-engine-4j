package de.uniks.vs.jalica.engine.idmanagement;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.UUID;

public class ID {

    private final UUID uuid;

    public ID() {
        uuid = generateUUIDV4();
    }

    public ID(String namespace, String name) {
        uuid = generateUUIDV3(namespace, name);
    }

    public String hyphenEliminated() {
        return uuid.toString().replace("-", "");
    }

    private UUID generateUUIDV4() {
        return UUID.randomUUID();
    }

    private UUID generateUUIDV3(String namespace, String name) {
        String source = namespace + name;
        byte[] bytes = new byte[0];
        try {
            bytes = source.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return UUID.nameUUIDFromBytes(bytes);
    }

    public long asLong() {
        long val = -1;

        do {
            final ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
            buffer.putLong(uuid.getLeastSignificantBits());
            buffer.putLong(uuid.getMostSignificantBits());
            final BigInteger bi = new BigInteger(buffer.array());
            val = bi.longValue();
        } while (val < 0);
        return val;
    }
}
