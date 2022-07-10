package org.max.rsync.delta;

import java.util.ArrayList;
import java.util.List;

public class Delta {

    private final List<String> diff = new ArrayList<>();

    public void addNewByte(byte newByte) {
        diff.add(String.valueOf((char) newByte));
    }

    public void addExistingChunk(int chunkId) {
        diff.add("[id:" + chunkId + "]");
    }
}
