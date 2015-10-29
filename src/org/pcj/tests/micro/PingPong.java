package org.pcj.tests.micro;

/*
 * @author Piotr
 */
import java.util.Locale;
import org.pcj.Group;
import org.pcj.PCJ;
import org.pcj.Shared;
import org.pcj.StartPoint;
import org.pcj.Storage;

public class PingPong extends Storage implements StartPoint {

    @Shared
    double[] a;

    @Override
    public void main() {
        if (PCJ.threadCount() < 2) {
            return;
        }
        Group g = null;
        if (PCJ.myId() <= 1) {
            g = PCJ.join("pingpong");
        }
        PCJ.barrier();

        if (g == null) {
            return;
        }

        int[] transmit
                = {
                    1, 10, 100, 1024, 2048, 4096, 8192, 16384,
                    32768, 65536, 131072, 262144, 524288, 1048576, 2097152,
                    4194304, //8388608, //16777216,
                };

        final int ntimes = 100;
        final int number_of_tests = 5;
        double[] b;

        for (int j = 0; j < transmit.length; j++) {
            int n = transmit[j];

            a = new double[n];
            b = new double[n];

            g.barrier();

            for (int i = 0; i < n; i++) {
                a[i] = (double) i + 1;
            }
            g.barrier();

            //get 
            double tmin_get = Double.MAX_VALUE;
            for (int k = 0; k < number_of_tests; k++) {
                long time = System.nanoTime();
                for (int i = 0; i < ntimes; i++) {
                    if (g.myId() == 0) {
                        b = g.get(1, "a");
                    }
                }
                time = System.nanoTime() - time;
                double dtime = (time / (double) ntimes) * 1e-9;

                g.barrier();
                if (tmin_get > dtime) {
                    tmin_get = dtime;
                }
            }
            g.barrier();

            // put
            PCJ.monitor("a"); // dodane
            g.barrier();
            for (int i = 0; i < n; i++) {
                a[i] = 0.0d;
                b[i] = (double) i + 1;
            }

            double tmin_put = Double.MAX_VALUE;
            for (int k = 0; k < number_of_tests; k++) {
                long time = System.nanoTime();
                for (int i = 0; i < ntimes; i++) {
                    if (g.myId() == 0) {
                        g.put(1, "a", b);
                    } else {
                        PCJ.waitFor("a");
                    }
                }

                time = System.nanoTime() - time;
                double dtime = (time / (double) ntimes) * 1e-9;

                g.barrier();
                if (tmin_put > dtime) {
                    tmin_put = dtime;
                }
            }

            // putB
            PCJ.monitor("a");
            g.barrier();
            for (int i = 0; i < n; i++) {
                b[i] = (double) i + 1;
            }

            double tmin_putB = Double.MAX_VALUE;
            for (int k = 0; k < number_of_tests; k++) {
                long time = System.nanoTime();
                for (int i = 0; i < ntimes; i++) {
                    if (g.myId() == i % 2) {
                        g.put((i + 1) % 2, "a", b);
                    } else {
                        PCJ.waitFor("a");
                    }
                }

                time = System.nanoTime() - time;
                double dtime = (time / (double) ntimes) * 1e-9;

                g.barrier();

                if (tmin_putB > dtime) {
                    tmin_putB = dtime;
                }
            }

            if (g.myId() == 0) {
                System.out.format(Locale.FRANCE, "PingPong\t%5d\tsize %12.7f\tt_get %12.7f\tt_put %12.7f\tt_putB %12.7f%n",
                        g.threadCount(), (double) n / 128, tmin_get, tmin_put, tmin_putB);
            }

        }
    }

//    public static void main(String[] args) {
//        String[] nodesTxt = new String[1024];
//        Scanner nf = null;
//        try {
//            nf = new Scanner(new File(args.length > 0 ? args[0] : "nodes.txt"));
//        } catch (FileNotFoundException ex) {
//            System.err.println("File not found!");
//        }
//
//        int n_nodes = 0;
//        if (nf != null) {
//            while (nf.hasNextLine()) {
//                nodesTxt[n_nodes] = nf.nextLine();
//                n_nodes++;
//            }
//        } else {
//            for (int i = 0; i < 2; ++i) {
//                nodesTxt[n_nodes] = "localhost:" + (8091 + i);
//                n_nodes++;
//            }
//        }
//
//        String[] nodes = new String[2];
//        nodes[0] = nodesTxt[0];
//        nodes[1] = nodesTxt[1];
//        PCJ.deploy(PingPong.class, PingPong.class, nodes);
//    }
}
