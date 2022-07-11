package org.max.rsync.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.max.rsync.io.ByteArray;
import org.max.rsync.delta.Delta;
import org.max.rsync.delta.DeltaCalculator;
import org.max.rsync.io.IOUtils;
import org.max.rsync.meta.FileMeta;
import org.max.rsync.meta.MetadataCalculator;
import org.max.rsync.meta.RollingHash;
import org.max.rsync.meta.StrongHash;

public class RsyncServer {

    private final MetadataCalculator metadataCalculator = new MetadataCalculator(new RollingHash(), new StrongHash());

    private final DeltaCalculator deltaCalculator = new DeltaCalculator(new RollingHash(), new StrongHash());

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

        long lastModifiedInFile = inFilePath.toFile().lastModified();
        long lastModifiedOutFile = outFilePath.toFile().lastModified();

        if( lastModifiedOutFile >= lastModifiedInFile ){
            System.out.printf("File '%s' is up-to-date, nothing to change%n", outFilePath.toFile().getName());
            return;
        }

        System.out.printf("Updating existing file '%s'%n", outFilePath.toFile().getName());

        FileMeta meta = readMetaFromFile(metaPath(outFilePath));

        Delta delta = deltaCalculator.calculateDelta(inFilePath, meta);

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

                for (Delta.DeltaChunk singleChange : delta.diff()) {
                    if (singleChange instanceof Delta.NewData newData) {
                        ByteArray byteArray = newData.getRawData();
                        out.write(byteArray.data(), 0, byteArray.length());
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
        System.out.printf("Creating new file '%s'%n", inFilePath.toFile().getName());

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
        try (OutputStream out = Files.newOutputStream(metaPath)) {
            FileMeta.writeToStream(fileMeta, out);
        }
    }

    private FileMeta readMetaFromFile(Path metaFilePath) throws IOException {
        try (InputStream in = Files.newInputStream(metaFilePath)) {
            return FileMeta.readFromStream(in);
        }
    }

    private Path metaPath(Path filePath) {
        return Path.of(filePath.getParent().toString(), ".meta-" + filePath.getFileName().toString());
    }

    private Path reconstructedFilePath(Path filePath) {
        return Path.of(filePath.getParent().toString(), ".reconstructed-" + filePath.getFileName().toString());
    }

}
