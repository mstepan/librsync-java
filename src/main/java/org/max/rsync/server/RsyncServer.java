package org.max.rsync.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.max.rsync.delta.Delta;
import org.max.rsync.delta.DiffCalculator;
import org.max.rsync.io.IOUtils;
import org.max.rsync.meta.FileMeta;
import org.max.rsync.meta.MetadataCalculator;
import org.max.rsync.meta.RollingHash;
import org.max.rsync.meta.Sha256Hash;

public class RsyncServer {

    private final MetadataCalculator metadataCalculator = new MetadataCalculator(new RollingHash(), new Sha256Hash());

    private final DiffCalculator diffCalculator = new DiffCalculator(new RollingHash(), new Sha256Hash());

    public void sync(Path inFolder, Path outFolder) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(inFolder)) {
            for (Path inPath : stream) {

                if (IOUtils.isFile(inPath)) {
                    Path outFilePath = outFolder.resolve(inPath.getFileName());

                    if (Files.exists(outFilePath)) {
                        updateExistingFile(inPath, outFilePath);
                    }
                    else {
                        saveFileWithChecksum(inPath, outFilePath);
                    }
                }
            }
        }
    }

    private void updateExistingFile(Path inFilePath, Path outFilePath) throws IOException {
        System.out.printf("Updating existing file: %s%n", outFilePath.toFile().getName());

        FileMeta meta = readMetaFromFile(metaPath(outFilePath));

        Delta delta = diffCalculator.calculateDelta(inFilePath, meta);

        reconstructFile(outFilePath, delta);

        recalculateMeta(outFilePath);
    }

    private void recalculateMeta(Path outFilePath) throws IOException {
        FileMeta newMeta;

        try (InputStream in = Files.newInputStream(outFilePath)) {
            newMeta = metadataCalculator.calculate(in);
        }

        Path metaPath = metaPath(outFilePath);
        Files.delete(metaPath);
        saveMetaFile(outFilePath, newMeta);
    }

    private void reconstructFile(Path outFilePath, Delta delta) throws IOException {

        Path tempPath = reconstructedFilePath(outFilePath);
        Files.createFile(tempPath);

        byte[] buf = new byte[MetadataCalculator.CHUNK_SIZE_IN_BYTES];

        try {
            try (var randomAccessFile = new RandomAccessFile(outFilePath.toFile(), "r");
                 var out = Files.newOutputStream(tempPath);
                 var baseFileIn = Files.newInputStream(outFilePath)) {

                for (Delta.DeltaChunk singleChange : delta.getDiff()) {
                    if (singleChange instanceof Delta.NewData newData) {
                        out.write(newData.ch());
                    }
                    else if (singleChange instanceof Delta.ExistingChunk existingChunk) {
                        int chunkId = existingChunk.id();
                        randomAccessFile.seek(((long) chunkId) * MetadataCalculator.CHUNK_SIZE_IN_BYTES);
                        int readBytes = randomAccessFile.read(buf);
                        out.write(buf, 0, readBytes);
                    }
                }
            }
            catch (FileNotFoundException ex) {
                throw new IllegalStateException(ex);
            }
        }
        finally {
            IOUtils.replaceFile(tempPath, outFilePath);
        }
    }

    public void saveFileWithChecksum(Path inFilePath, Path outFilePath) throws IOException {
        System.out.printf("Creating new file %s%n", inFilePath.toFile().getName());

        // create meta file
        try (InputStream in = Files.newInputStream(inFilePath, StandardOpenOption.READ)) {
            FileMeta fileMeta = metadataCalculator.calculate(in);
            saveMetaFile(outFilePath, fileMeta);
        }

        // create file copy
        Files.createFile(outFilePath);
        IOUtils.copyData(inFilePath, outFilePath);
    }

    private void saveMetaFile(Path outFilePath, FileMeta fileMeta) throws IOException {
        Path metaPath = metaPath(outFilePath);
        Files.createFile(metaPath);
        try (OutputStream out = Files.newOutputStream(metaPath); ObjectOutputStream objOut = new ObjectOutputStream(out)) {
            objOut.writeObject(fileMeta);
        }
    }

    private FileMeta readMetaFromFile(Path metaFilePath) throws IOException {
        try {
            try (InputStream in = Files.newInputStream(metaFilePath); ObjectInputStream objIn = new ObjectInputStream(in)) {
                return (FileMeta) objIn.readObject();
            }
        }
        catch (ClassNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private Path metaPath(Path filePath) {
        return Path.of(filePath.getParent().toString(), ".meta-" + filePath.getFileName().toString());
    }

    private Path reconstructedFilePath(Path filePath) {
        return Path.of(filePath.getParent().toString(), ".reconstructed-" + filePath.getFileName().toString());
    }

}
