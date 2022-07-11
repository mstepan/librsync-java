package org.max.rsync.delta;

import java.util.ArrayList;
import java.util.List;

public record Delta(List<DeltaChunk> diff) {

    Delta() {
        this(new ArrayList<>());
    }

    public void addNewByte(byte newByte) {

        DeltaChunk lastChunk = (diff.isEmpty()) ? null : diff.get(diff.size() - 1);

        if( lastChunk instanceof NewData newData){
            newData.append(newByte);
        }
        else {
            diff.add(new NewData(newByte));
        }
    }

    public void addExistingChunk(int chunkId) {
        diff.add(new ExistingChunk(chunkId));
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

        void append(byte newByte){
            rawData.append(newByte);
        }

    }
}
