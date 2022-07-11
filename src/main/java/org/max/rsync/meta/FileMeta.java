package org.max.rsync.meta;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

public final class FileMeta implements java.io.Externalizable {

    public static final long serialVersionUID = 2026391092388879077L;

    private List<ChunkMeta> chunkMetas;

    public FileMeta(){}

    public FileMeta(List<ChunkMeta> chunkMetas) {
        this.chunkMetas = chunkMetas;
    }

    public List<ChunkMeta> chunkMetas() {
        return chunkMetas;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        System.out.println("CUSTOM serialization used");

        out.writeInt(chunkMetas.size());

        for (ChunkMeta singleMeta : chunkMetas) {
            out.writeInt(singleMeta.id());
            out.writeInt(singleMeta.rollingHash());
            out.writeUTF(singleMeta.strongHash());
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        System.out.println("CUSTOM serialization used");

        chunkMetas = new ArrayList<>();

        final int chunksCount = in.readInt();

        for (int i = 0; i < chunksCount; ++i) {
            int id = in.readInt();
            int rollingHash = in.readInt();
            String strongHash = in.readUTF();
            chunkMetas.add(new ChunkMeta(id, rollingHash, strongHash));
        }
    }

}
