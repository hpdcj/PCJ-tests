package org.pcj.tests.micro;

/*
 * @author Piotr
 */
import org.pcj.Group;
import org.pcj.PCJ;
import org.pcj.Shared;
import org.pcj.StartPoint;

public class PingPong implements StartPoint {

    public enum SharedEnum implements Shared {
        a(double[].class);
        private final Class<?> type;

        private SharedEnum(Class<?> type) {
            this.type = type;
        }

        @Override
        public Class<?> type() {
            return type;
        }
    }

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
                    4194304, // 8388608, 16777216, 33554432, 67108864, 134217728, 268435440, 2147483647
                };

        final int ntimes = 100;
        final int number_of_tests = 5;

        for (int j = 0; j < transmit.length; j++) {
            int n = transmit[j];
            if (PCJ.myId() == 0) {
                System.err.println("n=" + n);
            }

            double[] a = new double[n];
            double[] b = new double[n];

            g.asyncBarrier().get();

            for (int i = 0; i < n; i++) {
                a[i] = (double) i + 1;
            }
            PCJ.putLocal(SharedEnum.a, a);
            g.asyncBarrier().get();

            //get 
            double tmin_get = Double.MAX_VALUE;
            for (int k = 0; k < number_of_tests; k++) {
                long time = System.nanoTime();
                for (int i = 0; i < ntimes; i++) {
                    if (g.myId() == 0) {
                        b = g.<double[]>asyncGet(1, SharedEnum.a).get();
                    }
                }
                time = System.nanoTime() - time;
                double dtime = (time / (double) ntimes) * 1e-9;

                g.asyncBarrier().get();
                if (tmin_get > dtime) {
                    tmin_get = dtime;
                }
            }
            g.asyncBarrier().get();

            // put
            for (int i = 0; i < n; i++) {
                a[i] = 0.0d;
                b[i] = (double) i + 1;
            }

            double tmin_put = Double.MAX_VALUE;
            for (int k = 0; k < number_of_tests; k++) {
                long time = System.nanoTime();
                for (int i = 0; i < ntimes; i++) {
                    if (g.myId() == 0) {
                        g.asyncPut(1, SharedEnum.a, b).get();
                    }
                }

                time = System.nanoTime() - time;
                double dtime = (time / (double) ntimes) * 1e-9;

                g.asyncBarrier().get();
                if (tmin_put > dtime) {
                    tmin_put = dtime;
                }
            }
            
            
            /* in putB we use waitFor, so we have to use PCJ.monitor to clear modification count */
            PCJ.monitor(SharedEnum.a);
            
            g.asyncBarrier().get();

            // putB
            for (int i = 0; i < n; i++) {
                b[i] = (double) i + 1;
            }

            double tmin_putB = Double.MAX_VALUE;
            for (int k = 0; k < number_of_tests; k++) {
                long time = System.nanoTime();
                for (int i = 0; i < ntimes; i++) {
                    if (g.myId() == i % 2) {
                        g.asyncPut((i + 1) % 2, SharedEnum.a, b);
                    } else {
                        PCJ.waitFor(SharedEnum.a);
                    }
                }

                time = System.nanoTime() - time;
                double dtime = (time / (double) ntimes) * 1e-9;

                g.asyncBarrier().get();
                if (tmin_putB > dtime) {
                    tmin_putB = dtime;
                }
            }

            if (g.myId() == 0) {
                System.out.format("PingPong\t%5d\tsize %12.7f\tt_get %12.7f\tt_put %12.7f\tt_putB %12.7f%n",
                        g.threadCount(), (double) n / 128, tmin_get, tmin_put, tmin_putB);
            }
//
        }
    }
}
