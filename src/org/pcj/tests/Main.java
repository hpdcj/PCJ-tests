/*
 * PCJ Test Benchmark Main File
 */
package org.pcj.tests;

import java.util.Arrays;
import org.pcj.PCJ;

/**
 *
 * @author faramir
 */
public class Main {

    public static void main(String[] args) throws Throwable {
        if (args.length == 0) {
            System.out.println("<start point> [nodes file] [num nodes]");
            System.exit(1);
        }

        MainArgs mainArgs = new MainArgs(args);

        if (mainArgs.getStartPoint() == null || mainArgs.getStorage() == null) {
            System.err.println("Unknown task: " + args[0]);
            System.exit(2);
        }

        try {
            PCJ.start(mainArgs.getStartPoint(), mainArgs.getStorage(), mainArgs.getNodes());
        } catch (NullPointerException ex) {
            System.err.println("Used nodes: " + Arrays.toString(mainArgs.getNodes()));
            throw ex;
        }
    }
}
