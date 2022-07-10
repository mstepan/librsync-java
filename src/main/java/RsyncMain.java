import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.max.rsync.meta.CalculateFileMetadata;
import org.max.rsync.meta.ChunkMeta;
import org.max.rsync.meta.FileMeta;
import org.max.rsync.meta.RollingHash;
import org.max.rsync.meta.Sha256Hash;

/**
 * JVM parameter to print memory when JVM process exited:
 * -XX:+UnlockDiagnosticVMOptions -XX:NativeMemoryTracking=summary -XX:+PrintNMTStatistics
 */
public class RsyncMain {

    public static void main(String[] args) throws Exception {

        CalculateFileMetadata metadataCalculator = new CalculateFileMetadata(new RollingHash(), new Sha256Hash());

        Path file = Path.of("/Users/mstepan/repo/librsync-java/sync-in/war-and-peace.txt");

        try (InputStream in = Files.newInputStream(file, StandardOpenOption.READ)) {
            FileMeta fileMeta = metadataCalculator.calculate(in);

            for (ChunkMeta chunkMeta : fileMeta.chunkMetas()) {
                System.out.println(chunkMeta);
            }
        }

        System.out.println("RsyncMain done...");
    }

}
