package org.max.rsync.meta;

public record ChunkMeta(int hash, String strongHash) implements java.io.Serializable {
}
