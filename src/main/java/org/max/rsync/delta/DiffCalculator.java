package org.max.rsync.delta;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.max.rsync.meta.MetadataCalculator;
import org.max.rsync.meta.ChunkMeta;
import org.max.rsync.meta.FileMeta;
import org.max.rsync.meta.RollingHash;
import org.max.rsync.meta.Sha256Hash;

public class DiffCalculator {

    private final RollingHash rollingHashCalculator;
    private final Sha256Hash strongHashCalculator;

    public DiffCalculator(RollingHash rollingHashCalculator, Sha256Hash strongHashCalculator) {
        this.rollingHashCalculator = Objects.requireNonNull(rollingHashCalculator);
        this.strongHashCalculator = Objects.requireNonNull(strongHashCalculator);
    }


    public Delta calculateDelta(Path inFilePath, FileMeta meta) throws IOException {

        Map<Integer, ChunkMeta> hashes = createMapOfHashes(meta);

        final byte[] window = new byte[MetadataCalculator.CHUNK_SIZE_IN_BYTES];

        final Delta delta = new Delta();

        try (InputStream in = Files.newInputStream(inFilePath)) {
            int readBytes = in.read(window);

            assert readBytes != -1 : "negative 'readBytes' detected, but should not";

            int curRollingHash = rollingHashCalculator.rollingHash(window, readBytes);

            while (true) {
                int existingChunkId = findChunkIdByChecksum(hashes, curRollingHash, window, readBytes);

                // chunk not found
                if (existingChunkId == -1) {

                    // add leftmost byte to delta
                    byte leftmostByte = window[0];
                    delta.addNewByte(leftmostByte);

                    // shift window one byte to the right
                    if (shiftRightOneByte(in, window)) {
                        byte rightmostByte = window[window.length - 1];
                        // recalculate rolling hash
                        curRollingHash = rollingHashCalculator.recalculate(curRollingHash, leftmostByte, readBytes,
                                                                           rightmostByte);

                    }
                    else {
                        // shift not possible, so just write all left bytes
                        for (int i = 1; i < readBytes; ++i) {
                            delta.addNewByte(window[i]);
                        }
                        break;
                    }
                }
                // existing chunk matched
                else {
                    delta.addExistingChunk(existingChunkId);
                    readBytes = in.read(window);
                    if (readBytes == -1) {
                        break;
                    }
                    curRollingHash = rollingHashCalculator.rollingHash(window, readBytes);
                }
            }
        }

        return delta;
    }

    private boolean shiftRightOneByte(InputStream in, byte[] window) throws IOException {
        int newByte = in.read();
        if (newByte == -1) {
            return false;
        }

        System.arraycopy(window, 1, window, 0, window.length - 1);
        window[window.length - 1] = (byte) newByte;

        return true;
    }

    private int findChunkIdByChecksum(Map<Integer, ChunkMeta> hashes, int rollingHash, byte[] window, int readBytes) {

        ChunkMeta curMeta = hashes.get(rollingHash);

        if (curMeta == null) {
            return -1;
        }

        if (curMeta.strongHash().equals(strongHashCalculator.sha256AsHex(window, readBytes))) {
            return curMeta.id();
        }

        return -1;
    }


    private Map<Integer, ChunkMeta> createMapOfHashes(FileMeta meta) {
        Map<Integer, ChunkMeta> hashes = new HashMap<>();

        for (ChunkMeta chunkMeta : meta.chunkMetas()) {
            hashes.put(chunkMeta.rollingHash(), chunkMeta);
        }
        return hashes;
    }

}
