package org.max.rsync.meta;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public final class StrongHash {

    private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static final String SHA_ALGORITHM = "SHA-256";

    /**
     * MessageDigest is not thread safe, so should be used as a separate field.
     */
    private final MessageDigest messageDigest;

    public StrongHash() {
        try {
            messageDigest = MessageDigest.getInstance(SHA_ALGORITHM);
        }
        catch (NoSuchAlgorithmException ex) {
            throw new ExceptionInInitializerError("Can't create message digest for " + SHA_ALGORITHM + ": " + ex.getMessage());
        }
    }

    public String hash(byte[] data, int length) {
        Objects.requireNonNull(data, "Can't calculate SHA rollingHash from null 'data' array");

        messageDigest.update(data, 0, length);

        byte[] digest = messageDigest.digest();
        return toHex(digest);
    }

    public String hash(byte[] data) {
        Objects.requireNonNull(data, "Can't calculate SHA rollingHash from null 'data' array");
        return hash(data, data.length);
    }

    private static String toHex(byte[] digest) {
        assert digest != null;

        StringBuilder buf = new StringBuilder(digest.length * 2);

        for (byte singleByte : digest) {
            buf.append(HEX_CHARS[(singleByte >> 4) & 0xF]);
            buf.append(HEX_CHARS[singleByte & 0xF]);
        }

        return buf.toString();
    }
}
