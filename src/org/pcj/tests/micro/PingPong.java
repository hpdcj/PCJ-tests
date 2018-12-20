/* 
 * Copyright (c) 2016, faramir
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.pcj.tests.micro;

/*
 * @author Piotr
 */
import org.pcj.Group;
import org.pcj.PCJ;
import org.pcj.StartPoint;
import org.pcj.Storage;
import org.pcj.tests.micro.PingPong.Shared;
import org.pcj.RegisterStorage;

@RegisterStorage(Shared.class)
public class PingPong implements StartPoint {

    @Storage(PingPong.class)
    public enum Shared {
        a
    }
    double[] a;

    @Override
    public void main() throws InterruptedException {
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
            System.gc();
            Thread.sleep(1000);

            int n = transmit[j];
            if (g.myId() == 0) {
                System.err.println("n=" + n);
            }

            a = new double[n];
            double[] b = new double[n];

            for (int i = 0; i < n; i++) {
                a[i] = (double) i + 1;
            }

            //get 
            PCJ.monitor(Shared.a);
            g.asyncBarrier().get();

            double tmin_get = Double.MAX_VALUE;
            for (int k = 0; k < number_of_tests; k++) {
                long time = System.nanoTime();
                for (int i = 0; i < ntimes; i++) {
                    if (g.myId() == 0) {
                        b = g.<double[]>asyncGet(1, Shared.a).get();
                    }
                }
                time = System.nanoTime() - time;
                double dtime = (time / (double) ntimes) * 1e-9;

                g.asyncBarrier().get();
                if (tmin_get > dtime) {
                    tmin_get = dtime;
                }
            }

            // put
            PCJ.monitor(Shared.a);
            g.asyncBarrier().get();

            for (int i = 0; i < n; i++) {
                a[i] = 0.0d;
                b[i] = (double) i + 1;
            }

            double tmin_put = Double.MAX_VALUE;
            for (int k = 0; k < number_of_tests; k++) {
                long time = System.nanoTime();
                for (int i = 0; i < ntimes; i++) {
                    if (g.myId() == 0) {
                        g.asyncPut(b, 1, Shared.a);
                    } else {
                        PCJ.waitFor(Shared.a);
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
            // putB
            PCJ.monitor(Shared.a);
            g.asyncBarrier().get();

            for (int i = 0; i < n; i++) {
                b[i] = (double) i + 1;
            }

            double tmin_putB = Double.MAX_VALUE;
            for (int k = 0; k < number_of_tests; k++) {
                long time = System.nanoTime();
                for (int i = 0; i < ntimes; i++) {
                    if (g.myId() == i % 2) {
                        g.asyncPut(b, (i + 1) % 2, Shared.a);
                    } else {
                        PCJ.waitFor(Shared.a);
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
