package org.max.rsync.wire;

public record NewChunk(int length, byte[] data) {
}
