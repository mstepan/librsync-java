package org.max.rsync.meta;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public record FileMeta(List<ChunkMeta> chunkMetas) {

    public static void writeToStream(FileMeta meta, OutputStream out) throws IOException {
        try (DataOutputStream dataOut = new DataOutputStream(out)) {
            dataOut.writeInt(meta.chunkMetas.size());

            for (ChunkMeta singleMeta : meta.chunkMetas) {
                dataOut.writeInt(singleMeta.id());
                dataOut.writeInt(singleMeta.rollingHash());
                dataOut.writeUTF(singleMeta.strongHash());
            }
        }
    }

    public static FileMeta readFromStream(InputStream in) throws IOException {
        List<ChunkMeta> chunksMeta = new ArrayList<>();

        try (DataInputStream dataIn = new DataInputStream(in)) {
            final int chunksCount = dataIn.readInt();

            for (int i = 0; i < chunksCount; ++i) {
                int id = dataIn.readInt();
                int rollingHash = dataIn.readInt();
                String strongHash = dataIn.readUTF();

                chunksMeta.add(new ChunkMeta(id, rollingHash, strongHash));
            }
        }
        return new FileMeta(chunksMeta);
    }
}
