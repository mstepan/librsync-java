package org.max.rsync.meta;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Weak rollingHash value with rolling window property.
 */
public class RollingHash {

    private static final int BASE = 256;

    private static final BigInteger BASE_BIG = BigInteger.valueOf(BASE);

    /**
     * Mod should be big prime value
     */
    private static final int MOD = 10061;

    private static final BigInteger MOD_BIG = BigInteger.valueOf(MOD);


    private static final int BASE_MOD_PRECALCULATED = baseInPowerMod(MetadataCalculator.CHUNK_SIZE_IN_BYTES);


    /**
     * Polynomial rolling hash. To prevent int overflow the polynomial value calculated by modulo.
     * For details check <a href="https://en.wikipedia.org/wiki/Rolling_hash">Rolling_hash</a>
     */
    public int rollingHash(byte[] data, int length) {
        Objects.requireNonNull(data, "Can't calculate SHA rollingHash from null 'data' array");

        int hash = 0;

        for (int i = 0; i < length; ++i) {
            int singleByte = data[i] & 0xFF;

            hash = ((hash * BASE) % MOD + singleByte) % MOD;
        }

        return hash;
    }

    public int recalculate(int curRollingHash, byte leftmostByte, int length, byte rightmostByte) {
        final int valToRemove = leftmostByte & 0xFF;
        final int valToAdd = rightmostByte & 0xFF;

        final int baseInPow =
            (length == MetadataCalculator.CHUNK_SIZE_IN_BYTES) ? BASE_MOD_PRECALCULATED : baseInPowerMod(length);

        int newRollingHash = curRollingHash - ((valToRemove * baseInPow) % MOD);

        if (newRollingHash < 0) {
            newRollingHash += MOD;
        }

        newRollingHash = (((newRollingHash * BASE) % MOD) + valToAdd) % MOD;

        return newRollingHash;
    }

    private static int baseInPowerMod(int power) {
        return BASE_BIG.modPow(BigInteger.valueOf(power), MOD_BIG).intValue();
    }
}
