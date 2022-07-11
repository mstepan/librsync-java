package org.max.rsync;

import java.nio.file.Path;
import org.max.rsync.server.RsyncServer;

/**
 * JVM parameter to print memory when JVM process exited:
 * -XX:+UnlockDiagnosticVMOptions -XX:NativeMemoryTracking=summary -XX:+PrintNMTStatistics
 */
public class RsyncMain {

    public static void main(String[] args) throws Exception {

        if (args.length < 2) {
            System.out.println("rsync should be called with 2 parameters: rsync <src-folder> <dest-folder>");
        }

        Path inFolder = Path.of(args[0]);
        Path outFolder = Path.of(args[1]);

        System.out.println("Rsync called for 2 folders: ");
        System.out.println("src ==> " + inFolder);
        System.out.println("dest ==> " + outFolder);

        RsyncServer rsync = new RsyncServer();
        rsync.sync(inFolder, outFolder);

        System.out.println("rsync completed...");
    }

}
