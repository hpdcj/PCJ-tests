package org.pcj.tests.micro;

/*
 * @author Piotr
 */
import java.util.Locale;
import org.pcj.PCJ;
import org.pcj.Shared;
import org.pcj.StartPoint;
import org.pcj.Storage;

public class Broadcast extends Storage implements StartPoint {

    @Shared
    double[] a;

    @Override
    public void main() {

        int[] transmit
                = {
                    1, 10, 100, 1024, 2048, 4096, 8192, 16384,
                    32768, 65536, 131072, 262144, 524288, 1048576, 2097152,
                    4194304, //8388608, //16777216,
                };

        for (int n : transmit) {
            PCJ.barrier();

            double[] b = new double[n];
            for (int i = 0; i < n; i++) {
                b[i] = (double) i + 1;
            }
            PCJ.monitor("a");

            PCJ.barrier();

            int ntimes = 100;

            long time = System.nanoTime();

            for (int i = 0; i < ntimes; i++) {
                if (PCJ.myId() == 0) {
                    PCJ.broadcast("a", b);
                }
                PCJ.waitFor("a");
                PCJ.barrier();
            }

            time = System.nanoTime() - time;
            double dtime = (time / (double) ntimes) * 1e-9;
            PCJ.barrier();

            if (PCJ.myId() == 0) {
                System.out.format(Locale.FRANCE, "%5d\tsize %12.7f\ttime %12.7f%n",
                        PCJ.threadCount(), (double) n * 8 / 1024, dtime);
            }
        }
    }

//    public static void main(String[] args) {
//        String nodesDescription = "nodes.uniq";
//        if (args.length > 0) {
//            nodesDescription = args[0];
//        }
//        Scanner nf = null;
//        try {
//            nf = new Scanner(new File(nodesDescription));
//        } catch (FileNotFoundException ex) {
//            System.err.println("File not found: " + nodesDescription);
//        }
//
//        //        int[] threads = {1, 4, 16};
//        int[] threads = {1, 2, 4, 8, 12};
//
//        String[] nodesUniq = new String[1024];
//
//        int n_nodes = 0;
//        if (nf != null) {
//            while (nf.hasNextLine()) {
//                nodesUniq[n_nodes] = nf.nextLine() + ":9100";
//                System.out.println(nodesUniq[n_nodes]);
//                n_nodes++;
//            }
//        } else {
//            for (int i = 0; i < 5; ++i) {
//                nodesUniq[n_nodes] = "localhost:910" + i;
//                n_nodes++;
//            }
//        }
//
//        for (int m = n_nodes; m > 0; m = m / 2) {
//            int nn = m;
//
//            for (int nt : threads) {
//                String[] nodes = new String[nt * nn];
//                System.out.println(" Start deploy nn=" + nn + " nt=" + nt);
//                int ii = 0;
//                for (int i = 0; i < nn; i++) {
//                    for (int j = 0; j < nt; j++) {
//                        nodes[ii] = nodesUniq[i];
//                        ii++;
//
//                    }
//                }
//                PCJ.deploy(Broadcast.class, Broadcast.class, nodes);
//            }
//        }
//    }
}
