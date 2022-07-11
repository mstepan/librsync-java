package org.max.rsync.delta;

import java.util.ArrayList;
import java.util.List;
import org.max.rsync.io.ByteArray;

public final class Delta {

    private final List<DeltaChunk> diff = new ArrayList<>();
    private DeltaChunk lastAddedChunk;

    public List<DeltaChunk> diff() {
        return diff;
    }

    public void addNewByte(byte newByte) {
        if (lastAddedChunk instanceof NewData newData) {
            newData.append(newByte);
        }
        else {
            lastAddedChunk = new NewData(newByte);
            diff.add(lastAddedChunk);
        }
    }

    public void addExistingChunk(int chunkId) {
        lastAddedChunk = new ExistingChunk(chunkId);
        diff.add(lastAddedChunk);
    }


    public interface DeltaChunk {
    }

    public record ExistingChunk(int id) implements DeltaChunk {
    }

    public static final class NewData implements DeltaChunk {

        private final ByteArray rawData = new ByteArray();

        public NewData(byte newByte) {
            rawData.append(newByte);
        }

        public ByteArray getRawData() {
            return rawData;
        }

        void append(byte newByte) {
            rawData.append(newByte);
        }

    }
}
