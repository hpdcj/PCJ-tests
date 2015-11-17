/*
 * PCJ Test Benchmark Main File
 */
package org.pcj.tests;

import org.pcj.PCJ;

/**
 *
 * @author faramir
 */
public class MainWithDeploy {

    public static void main(String[] args) throws Throwable {
        if (args.length == 0) {
            System.out.println("<start point> [nodes file] [num nodes]");
//            args = new String[]{"EasyTest"};
//            args = new String[]{"Barrier"};
//            args = new String[]{"Broadcast"};
//            args = new String[]{"PingPong"};
//            args = new String[]{"PiInt"};
//            args = new String[]{"PiMC"};
//            args = new String[]{"RayTracerA"};
//            args = new String[]{"RayTracerB"};
//            args = new String[]{"RayTracerC"};
//            args = new String[]{"RayTracerD"};
//            args = new String[]{"MolDynA"};
//            args = new String[]{"MolDynB"};
//            args = new String[]{"MolDynC"};
//            args = new String[]{"MolDynD"};
//            args = new String[]{"MolDynE"};
            System.exit(1);
        }

        MainArgs mainArgs = new MainArgs(args);

        if (mainArgs.getStartPoint() == null || mainArgs.getStorage() == null) {
            System.err.println("Unknown task: " + args[0]);
            System.exit(2);
        }

        PCJ.deploy(mainArgs.getStartPoint(), mainArgs.getStorage(), mainArgs.getNodes());
    }
}
