package org.max.rsync.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

public final class IOUtils {

    private IOUtils() {
        throw new AssertionError("Can't instantiate utility-only class");
    }

    public static void replaceFile(Path srcPath, Path destPath) throws IOException {
        Files.move(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    public static void copyData(Path inFilePath, Path outFilePath) throws IOException {
        try (InputStream inStream = Files.newInputStream(inFilePath, StandardOpenOption.READ);
             OutputStream outStream = Files.newOutputStream(outFilePath)) {

            byte[] data = new byte[1024];

            int readBytes;

            while ((readBytes = inStream.read(data)) != -1) {
                outStream.write(data, 0, readBytes);
            }
        }
    }

    public static boolean isFile(Path path) {
        return !Files.isDirectory(path);
    }

}
