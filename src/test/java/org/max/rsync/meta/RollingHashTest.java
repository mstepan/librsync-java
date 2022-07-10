package org.max.rsync.meta;

import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class RollingHashTest {

    @Test
    public void hashData() {
        RollingHash hash = new RollingHash();

        byte[] data = "hello world".getBytes(StandardCharsets.UTF_8);
        assertEquals(8520, hash.rollingHash(data, data.length));
    }
}
