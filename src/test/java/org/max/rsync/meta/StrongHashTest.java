package org.max.rsync.meta;

import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class StrongHashTest {

    @Test
    public void hashData() {
        StrongHash calc = new StrongHash();
        String actualHash = calc.hash("hello world".getBytes(StandardCharsets.UTF_8));
        assertEquals("B94D27B9934D3E08A52E52D7DA7DABFAC484EFE37A5380EE9088F7ACE2EFCDE9", actualHash);
    }

    /**
     * <a href="https://www.movable-type.co.uk/scripts/sha256.html">Use this link to check you SHA256 hashes</a>
     */
    @Test
    public void hashPredefinedData() {
        StrongHash calc = new StrongHash();
        String actualHash = calc.hash("some arbitrary length string".getBytes(StandardCharsets.UTF_8));
        assertEquals("b1cf0304a6c115e9befac49ca6e78f26a690fd2cf36a9203734c6c611bf042e1".toUpperCase(), actualHash);
    }

    @Test
    public void hashEmptyArrayShouldBeOk() {
        StrongHash calc = new StrongHash();
        String actualHash = calc.hash("".getBytes(StandardCharsets.UTF_8));
        assertEquals("E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855", actualHash);
    }

    @Test
    public void hashFromBufferWithLimit() {
        StrongHash calc = new StrongHash();

        final String usefulPart = "hello world";
        final String partToIgnore = " this part should be ignored";
        final int length = usefulPart.length();

        String actualHash = calc.hash((usefulPart + partToIgnore).getBytes(StandardCharsets.UTF_8), length);
        assertEquals("B94D27B9934D3E08A52E52D7DA7DABFAC484EFE37A5380EE9088F7ACE2EFCDE9", actualHash);
    }

    @Test
    public void hashForNullDataShouldFail() {
        StrongHash calc = new StrongHash();
        Throwable ex = assertThrows(NullPointerException.class, () -> calc.hash(null));
        assertEquals("Can't calculate SHA rollingHash from null 'data' array", ex.getMessage());
    }

    @Test
    public void checkHashConsistent() {
        StrongHash calc = new StrongHash();
        String hash1 = calc.hash("check rollingHash consistent".getBytes(StandardCharsets.UTF_8));
        String hash2 = calc.hash("check rollingHash consistent".getBytes(StandardCharsets.UTF_8));
        assertEquals(hash1, hash2);
    }


}
