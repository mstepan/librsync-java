import java.nio.file.Path;
import org.max.rsync.server.RsyncServer;

/**
 * JVM parameter to print memory when JVM process exited:
 * -XX:+UnlockDiagnosticVMOptions -XX:NativeMemoryTracking=summary -XX:+PrintNMTStatistics
 */
public class RsyncMain {

    public static void main(String[] args) throws Exception {

        Path inFolder = Path.of("/Users/mstepan/repo/librsync-java/sync-in");
        Path outFolder = Path.of("/Users/mstepan/repo/librsync-java/sync-out");

        RsyncServer rsync = new RsyncServer();
        rsync.sync(inFolder, outFolder);

        System.out.println("RsyncMain done...");
    }

}
