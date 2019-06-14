package org.pcj.tests.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import org.pcj.PCJ;
import org.pcj.RegisterStorage;
import org.pcj.StartPoint;
import org.pcj.Storage;

@RegisterStorage(IoTest.Shared.class)
public class IoTest implements StartPoint {

    @Storage(IoTest.class)
    enum Shared {
        elapsedTimeArray,
        readBytes
    }

    long[] elapsedTimeArray = PCJ.myId() == 0 ? new long[PCJ.threadCount()] : null;
    long[] readBytes = PCJ.myId() == 0 ? new long[PCJ.threadCount()] : null;

    public static void main(String[] args) throws IOException {
        String nodesFile = System.getProperty("nodes", "nodes.txt");
        String filename = System.getProperty("file", "");
        if (new File(filename).isFile() == false) {
            System.err.println("'" + filename + "': not a file");
            System.exit(1);
        }
        PCJ.executionBuilder(IoTest.class)
                .addNodes(new File(nodesFile))
                .start();
    }

    @Override
    public void main() throws Throwable {
        long start, end;
        String filename = System.getProperty("file", "");
        System.err.println("Reading file: " + filename);

        start = System.nanoTime();
        mesaureReadFile(filename);
        end = System.nanoTime();
        System.err.printf("Warming up finished after %d ns\n", end - start);
        PCJ.barrier();

        start = System.nanoTime();
        long length = mesaureReadFile(filename);
        end = System.nanoTime();

        System.err.printf("%d\t%d\t%d\t%d\tns\t%d\tB\t%s\n",
                PCJ.getNodeCount(),
                PCJ.threadCount(),
                PCJ.myId(),
                (end - start),
                length,
                filename);

        PCJ.put(end - start, 0, IoTest.Shared.elapsedTimeArray, PCJ.myId());
        PCJ.put(length, 0, IoTest.Shared.readBytes, PCJ.myId());
        if (PCJ.myId() == 0) {
            PCJ.waitFor(IoTest.Shared.elapsedTimeArray, PCJ.threadCount());
            long totalTime = Arrays.stream(elapsedTimeArray).max().orElse(Long.MAX_VALUE);

            PCJ.waitFor(IoTest.Shared.readBytes, PCJ.threadCount());
            long totalBytes = Arrays.stream(readBytes).sum();

            System.out.printf("%d\t%d\t%.3f\ts\t%.1f\tMB\t%.3f\tMB/s\t%s\t%s\n",
                    PCJ.getNodeCount(),
                    PCJ.threadCount(),
                    totalTime / 1e9,
                    (double) totalBytes / (1 << 20),
                    (double) totalBytes / (1 << 20) / (totalTime / 1e9),
                    filename,
                    Arrays.toString(elapsedTimeArray));
        }
    }

    private long mesaureReadFile(String filename) throws IOException {
        long length = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                length += line.length() + 1;
            }
        }
        return length;
    }
}
