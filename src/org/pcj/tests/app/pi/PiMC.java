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
package org.pcj.tests.app.pi;

import java.util.Arrays;
import java.util.Random;
import org.pcj.PCJ;
import org.pcj.PcjFuture;
import org.pcj.PcjRuntimeException;
import org.pcj.StartPoint;
import org.pcj.Storage;
import org.pcj.tests.app.pi.PiMC.Shared;
import org.pcj.RegisterStorage;

@RegisterStorage(Shared.class)
public class PiMC implements StartPoint {

    private final Random random = new Random();

    @Storage(PiMC.class)
    public enum Shared {
        circleCount
    }
    long circleCount;

    @Override
    public void main() {
        long pointsAll = Long.parseLong(System.getProperty("PiMC.pointsAll", "600000000"));
        long points = pointsAll / PCJ.threadCount();
        if (PCJ.myId() == 0) {
            System.err.println("PiMC on " + pointsAll + " points");
        }

        int number_of_tests = 5;
        double[] times = new double[number_of_tests];

        double pi = 0.0;

        for (int i = 0; i < number_of_tests; ++i) {
            PCJ.barrier();

            long time = System.nanoTime();
            pi = calculate(points);
            double dtime = (System.nanoTime() - time) / 1e9;

            times[i] = dtime;
            if (PCJ.myId() == 0) {
                System.err.printf("PiMC\t%5d\ttime %12.7f%n", PCJ.threadCount(), dtime);
            }
        }
        // Print results         
        if (PCJ.myId() == 0) {
            validate(pi);

            System.out.format("PiMC\t%5d\tpointsAll %d\ttime %12.7f\ttimes=%s%n",
                    PCJ.threadCount(), pointsAll,
                    Arrays.stream(times).min().getAsDouble(), Arrays.toString(times));
        }

    }

    private double calculate(long n) throws PcjRuntimeException {
        circleCount = 0;

        // Calculate  
        for (long i = 0; i < n; ++i) {
            double x = 2.0 * random.nextDouble() - 1.0;
            double y = 2.0 * random.nextDouble() - 1.0;
            if ((x * x + y * y) < 1.0) {
                circleCount++;
            }
        }

        PCJ.barrier();

        // Gather results 
        if (PCJ.myId() == 0) {
            long c = 0;
            PcjFuture<Long> cL[] = new PcjFuture[PCJ.threadCount()];

            for (int p = 0; p < PCJ.threadCount(); p++) {
                cL[p] = PCJ.asyncGet(p, Shared.circleCount);
            }
            for (int p = 0; p < PCJ.threadCount(); p++) {
                c = c + (long) cL[p].get();
            }

            // Calculate pi 
            return 4.0 * (double) c / ((double) n * PCJ.threadCount());
        }

        return Double.NaN;
    }

    private void validate(double pi) {
        double refval = Math.PI;
        double dev = Math.abs(pi - refval);

        if (dev > 1.0e-4) {
            System.err.println("Validation failed");
            System.err.println("Value = " + pi + "  " + dev);
        }
    }
}
