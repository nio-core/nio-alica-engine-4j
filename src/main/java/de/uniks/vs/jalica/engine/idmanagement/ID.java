package de.uniks.vs.jalica.engine.idmanagement;

import de.uniks.vs.jalica.common.utils.CommonUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.sql.SQLOutput;
import java.util.UUID;

public class ID {

    private final UUID uuid;

    private long longID = -1;

    public ID() {
        uuid = generateUUIDV4();
        longID = this.getLongID();
    }

    public ID(String namespace, String name) {
        uuid = generateUUIDV3(namespace, name);
        longID = this.getLongID();
    }

    public long asLong() {
        return this.longID;
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

    private long getLongID() {
        long longID = uuid.getMostSignificantBits() & Long.MAX_VALUE;
        if (longID < 0) CommonUtils.aboutError("Negative long ID");
        return longID;
//        long longID = -1;
//
//        while (longID < 0) {
//            final ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
//            buffer.putLong(uuid.getLeastSignificantBits());
//            buffer.putLong(uuid.getMostSignificantBits());
//            final BigInteger bi = new BigInteger(buffer.array());
//            val = bi.longValue();
//            System.out.println(val);
//            longID = uuid.getMostSignificantBits() & Long.MAX_VALUE;
//            System.out.println(val);
//        }
    }

    @Override
    public String toString() {
        return String.valueOf(this.longID);
    }
}
