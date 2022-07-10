package org.max.rsync.meta;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public final class Sha256Hash {

    private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static final String SHA_ALGORITHM = "SHA-256";

    private final MessageDigest messageDigest;

    public Sha256Hash() {
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException ex) {
            throw new ExceptionInInitializerError("Can't create message digest for " + SHA_ALGORITHM + ": " + ex.getMessage());
        }
    }

    public String sha256AsHex(byte[] data, int length) {
        Objects.requireNonNull(data, "Can't calculate SHA hash from null 'data' array");

        messageDigest.update(data, 0, length);

        byte[] digest = messageDigest.digest();
        return toHex(digest);
    }

    public String sha256AsHex(byte[] data) {
        Objects.requireNonNull(data, "Can't calculate SHA hash from null 'data' array");
        return sha256AsHex(data, data.length);
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

    public static void main(String[] args) {
        Sha256Hash calc = new Sha256Hash();
        String res = calc.sha256AsHex("hello world".getBytes(StandardCharsets.UTF_8));
        System.out.println(res);
    }

}
