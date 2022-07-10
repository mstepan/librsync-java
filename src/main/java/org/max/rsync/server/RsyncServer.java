package org.max.rsync.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.max.rsync.io.IOUtils;
import org.max.rsync.meta.CalculateFileMetadata;
import org.max.rsync.meta.FileMeta;
import org.max.rsync.meta.RollingHash;
import org.max.rsync.meta.Sha256Hash;

public class RsyncServer {

    private final CalculateFileMetadata metadataCalculator =
        new CalculateFileMetadata(new RollingHash(), new Sha256Hash());

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

    private void updateExistingFile(Path inFilePath, Path outFilePath) {
        System.out.printf("Updating existing file: %s%n", outFilePath.toFile().getName());
        //TODO:
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
        Path metaPath = Path.of(outFilePath.getParent().toString(), ".meta-" + outFilePath.getFileName().toString());
        Files.createFile(metaPath);
        try (OutputStream out = Files.newOutputStream(metaPath); ObjectOutputStream objOut = new ObjectOutputStream(out)) {
            objOut.writeObject(fileMeta);
        }
    }

}
