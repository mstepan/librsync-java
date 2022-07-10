package org.max.rsync.meta;

import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class Sha256HashTest {

    @Test
    public void hashData() {
        Sha256Hash calc = new Sha256Hash();
        String actualHash = calc.sha256AsHex("hello world".getBytes(StandardCharsets.UTF_8));
        assertEquals("B94D27B9934D3E08A52E52D7DA7DABFAC484EFE37A5380EE9088F7ACE2EFCDE9", actualHash);
    }

    /**
     * <a href="https://www.movable-type.co.uk/scripts/sha256.html">Use this link to check you SHA256 hashes</a>
     */
    @Test
    public void hashPredefinedData() {
        Sha256Hash calc = new Sha256Hash();
        String actualHash = calc.sha256AsHex("some arbitrary length string".getBytes(StandardCharsets.UTF_8));
        assertEquals("b1cf0304a6c115e9befac49ca6e78f26a690fd2cf36a9203734c6c611bf042e1".toUpperCase(), actualHash);
    }

    @Test
    public void hashEmptyArrayShouldBeOk() {
        Sha256Hash calc = new Sha256Hash();
        String actualHash = calc.sha256AsHex("".getBytes(StandardCharsets.UTF_8));
        assertEquals("E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855", actualHash);
    }

    @Test
    public void hashFromBufferWithLimit() {
        Sha256Hash calc = new Sha256Hash();

        final String usefulPart = "hello world";
        final String partToIgnore = " this part should be ignored";
        final int length = usefulPart.length();

        String actualHash = calc.sha256AsHex((usefulPart + partToIgnore).getBytes(StandardCharsets.UTF_8), length);
        assertEquals("B94D27B9934D3E08A52E52D7DA7DABFAC484EFE37A5380EE9088F7ACE2EFCDE9", actualHash);
    }

    @Test
    public void hashForNullDataShouldFail() {
        Sha256Hash calc = new Sha256Hash();
        Throwable ex = assertThrows(NullPointerException.class, () -> calc.sha256AsHex(null));
        assertEquals("Can't calculate SHA rollingHash from null 'data' array", ex.getMessage());
    }

    @Test
    public void checkHashConsistent() {
        Sha256Hash calc = new Sha256Hash();
        String hash1 = calc.sha256AsHex("check rollingHash consistent".getBytes(StandardCharsets.UTF_8));
        String hash2 = calc.sha256AsHex("check rollingHash consistent".getBytes(StandardCharsets.UTF_8));
        assertEquals(hash1, hash2);
    }


}
