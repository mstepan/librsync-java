package org.max.rsync.meta;

import java.util.List;

public record FileMeta(List<ChunkMeta> chunkMetas) implements java.io.Serializable {


}
