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
package org.pcj.tests.app;

import java.util.Random;
import org.pcj.PCJ;
import org.pcj.PcjFuture;
import org.pcj.RegisterStorages;
import org.pcj.StartPoint;
import org.pcj.Storage;
import org.pcj.tests.app.PiMC.Shared;

@RegisterStorages(Shared.class)
public class PiMC implements StartPoint {

    @Storage(PiMC.class)
    public enum Shared {
        circleCount
    }
    long circleCount;

    @Override
    public void main() {
        Random random = new Random();
        long nAll = 512_000_000;
        long n = nAll / PCJ.threadCount();

        circleCount = 0;
        long time = System.nanoTime();

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
        long c = 0;
        PcjFuture<Long> cL[] = new PcjFuture[PCJ.threadCount()];

        if (PCJ.myId() == 0) {
            for (int p = 0; p < PCJ.threadCount(); p++) {
                cL[p] = PCJ.asyncGet(p, Shared.circleCount);
            }
            for (int p = 0; p < PCJ.threadCount(); p++) {
                c = c + (long) cL[p].get();
            }
        }

        // Calculate pi 
        double pi = 4.0 * (double) c / (double) nAll;

        time = System.nanoTime() - time;
        double dtime = time * 1e-9;

        // Print results         
        if (PCJ.myId() == 0) {
            validate(pi);

            System.out.format("PiMC\t%5d\ttime %12.7f%n",
                    PCJ.threadCount(), dtime);
        }
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
