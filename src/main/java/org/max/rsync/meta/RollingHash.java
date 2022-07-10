package org.max.rsync.meta;

import java.util.Objects;

/**
 * Weak hash value with rolling window property.
 */
public class RollingHash {

    private static final int BASE = 256;

    /**
     * Mod should be big prime value
      */
    private static final int MOD = 10061;

    public int rollingHash(byte[] data, int length) {
        Objects.requireNonNull(data, "Can't calculate SHA hash from null 'data' array");

        long hash = 0L;

        for (int i = 0; i < length; ++i) {
            int singleByte = data[i] & 0xFF;

            hash = ((hash * BASE) % MOD + singleByte) % MOD;
        }

        return (int)hash;
    }
}
