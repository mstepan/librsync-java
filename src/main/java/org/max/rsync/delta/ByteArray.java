package org.max.rsync.delta;


import java.util.Arrays;

/**
 * Resizable byte array implementation, similar to ArrayList, but for byte[] array.
 */
public final class ByteArray {

    private byte[] data = new byte[1024];

    private int length;

    public void append(byte newByte) {
        if (length == data.length) {
            resize();
        }

        data[length] = newByte;
        ++length;
    }

    public byte[] data() {
        return data;
    }

    public int length() {
        return length;
    }

    private void resize() {
        data = Arrays.copyOf(data, data.length + (data.length >> 1));
    }
}
