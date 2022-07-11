package org.max.rsync.delta;

import java.util.ArrayList;
import java.util.List;

public class Delta {

    private final List<DeltaChunk> diff = new ArrayList<>();

    public List<DeltaChunk> getDiff() {
        return diff;
    }

    public void addNewByte(byte newByte) {
        diff.add(new NewData((char) newByte));
    }

    public void addExistingChunk(int chunkId) {
        diff.add(new ExistingChunk(chunkId));
    }


    public interface DeltaChunk {
    }

    public record ExistingChunk(int id) implements DeltaChunk {
    }

    public record NewData(char ch) implements DeltaChunk {
    }
}
