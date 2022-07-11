package org.max.rsync.meta;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MetadataCalculator {

    // TODO: temporary set for 128 bytes, should be something like 1MB
    public static final int CHUNK_SIZE_IN_BYTES = 128;

    private final RollingHash rollingHash;
    private final Sha256Hash sha256Hash;

    public MetadataCalculator(RollingHash rollingHash, Sha256Hash sha256Hash) {
        this.rollingHash = rollingHash;
        this.sha256Hash = sha256Hash;
    }

    public FileMeta calculate(InputStream in) {

        final List<ChunkMeta> chunkMetas = new ArrayList<>();

        final byte[] buf = new byte[CHUNK_SIZE_IN_BYTES];

        int readBytes;
        int id = 0;

        try {
            while ((readBytes = in.read(buf)) != -1) {
                chunkMetas.add(calculateChunkMetadata(id, buf, readBytes));
                ++id;
            }
        }
        catch (IOException ioEx) {
            throw new IllegalStateException("Can't read from sync-in stream", ioEx);
        }
        return new FileMeta(chunkMetas);
    }

    private ChunkMeta calculateChunkMetadata(int id, byte[] buf, int length) {
        int weakHash = rollingHash.rollingHash(buf, length);
        String strongHash = this.sha256Hash.sha256AsHex(buf, length);

        return new ChunkMeta(id, weakHash, strongHash);
    }


}
