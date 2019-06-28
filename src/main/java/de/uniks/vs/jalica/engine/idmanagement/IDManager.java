package de.uniks.vs.jalica.engine.idmanagement;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.locks.Lock;

public class IDManager {

    static HashMap<ID, Object> uuids = new HashMap();
    static HashSet<Long> uids = new HashSet<>();
    HashMap<Identifier, Integer> ids;
    Lock idsMutex;


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

    public long getIDFromBytes(byte idBytes, int idSize, byte type) {

        if (idBytes == 0) {
            return -1;
        }

        // create tmpID for lookup the ID
        Identifier tmpID = new Identifier(idBytes, idSize, type);

        synchronized (this) {
            // lookup the ID and insert it, if not available, yet
            if (!this.ids.containsKey(tmpID)) { // delete tmpID if already present in IDs
                this.ids.put(tmpID, getIntValue(idBytes));
            }
        }
        return this.ids.get(tmpID);
    }

    private Integer getIntValue(byte idBytes) {
        return idBytes & 0xff;
    }

    private class Identifier {

        // int value = eightBits & 0xff;

        byte TYPE;

        int INT_TYPE = 0;
        int WILDCARD_TYPE = 1;
        int UUID_TYPE = 2;

        ArrayList<Integer> id;

        public Identifier(byte idBytes, int idSize, byte type) {
            this.TYPE = type;
            id = new ArrayList<>();

            for (int i = 0; i < idSize; i++) {
                this.id.add((idBytes >> i) & 1);
            }
        }


        int getType() {
            return this.TYPE;
        }

        boolean quals(Identifier other) {
            if (this.id.size() != other.id.size()) {
                return false;
            }
            for (int i = 0; i < this.id.size(); i++) {
                if (this.id.get(i) != other.id.get(i)) {
                    return false;
                }
            }
            return true;
        }


        boolean lessThan(Identifier other) {
            if (this.id.size() < other.id.size()) {
                return true;
            } else if (this.id.size() > other.id.size()) {
                return false;
            }
            for (int i = 0; i < this.id.size(); i++) {
                if (this.id.get(i) < other.id.get(i)) {
                    return true;
                } else if (this.id.get(i) > other.id.get(i)) {
                    return false;
                }
                // else continue, because both bytes where equal and the next byte needs to be considered
            }
            return false;
        }

        boolean graterThan(Identifier other) {
            if (this.id.size() > other.id.size()) {
                return true;
            } else if (this.id.size() < other.id.size()) {
                return false;
            }
            for (int i = 0; i < this.id.size(); i++) {
                if (this.id.get(i) > other.id.get(i)) {
                    return true;
                } else if (this.id.get(i) < other.id.get(i)) {
                    return false;
                }
                // else continue, because both bytes where equal and the next byte needs to be considered
            }
            return false;
        }

        ArrayList<Integer> getRaw() {
            return this.id;
        }

        int getSize() {
            return this.id.size();
        }

        ArrayList<Integer> toByteVector() {
            return this.id;
        }

        //TODO: finish implementation
        /**
         * See:
         * https://en.wikipedia.org/wiki/MurmurHash
         * https://softwareengineering.stackexchange.com/questions/49550/which-hashing-algorithm-is-best-for-uniqueness-and-speed
         */
        public static final long JCOMMON_SEED = 1318007700;
        private long seed = JCOMMON_SEED;

        public long hash(long data) {
            long c1 = 0x87c37b91114253d5L;
            long c2 = 0x4cf5ad432745937fL;

            long h1 = seed, h2 = seed;

            long k1 = data;
            k1 *= c1;
            k1 = rotateLeft64(k1, 31);
            k1 *= c2;
            h1 ^= k1;

            h1 ^= 8;
            h2 ^= 8;

            h1 += h2;
            h2 += h1;

            return (fmix(h1) + fmix(h2));
        }


        private long rotateLeft64(long x, int r) {
            return (x << r) | (x >>> (64 - r));
        }

        private long fmix(long k) {
            k ^= k >>> 33;
            k *= 0xff51afd7ed558ccdL;
            k ^= k >>> 33;
            k *= 0xc4ceb9fe1a85ec53L;
            k ^= k >>> 33;

            return k;
        }
    }
}
