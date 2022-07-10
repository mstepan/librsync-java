package org.max.rsync.meta;

public record ChunkMeta(int id, int rollingHash, String strongHash) implements java.io.Serializable {
}
