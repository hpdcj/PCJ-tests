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

import org.pcj.PCJ;
import org.pcj.PcjFuture;
import org.pcj.Shared;
import org.pcj.StartPoint;

/**
 *
 * @author faramir
 */
public class PiInt implements StartPoint {

    public enum SharedEnum implements Shared {
        sum(double.class);
        private final Class<?> type;

        private SharedEnum(Class<?> type) {
            this.type = type;
        }

        @Override
        public Class<?> type() {
            return type;
        }
    }

    private double f(double x) {
        return (4.0 / (1.0 + x * x));
    }

    @Override
    public void main() throws Throwable {
        int ntimes = 100;
        int points = 100_000_000;

        double pi = 0.0;

        long time = System.nanoTime();

        for (int i = 0; i < ntimes; ++i) {
            pi = calc(points);
            PCJ.barrier();
        }

        time = System.nanoTime() - time;
        double dtime = time * 1e-9;

        if (PCJ.myId() == 0) {
            validate(pi);

            System.out.format("PiInt\t%5d\ttime %12.7f%n",
                    PCJ.threadCount(), dtime);
        }
    }

    private double calc(int N) {
        double w;

        w = 1.0 / (double) N;
        double sum = 0.0;
        for (int i = PCJ.myId() + 1; i <= N; i += PCJ.threadCount()) {
            sum = sum + f(((double) i - 0.5) * w);
        }
        sum = sum * w;
        PCJ.putLocal(SharedEnum.sum, sum);

        PCJ.barrier();
        if (PCJ.myId() == 0) {
            PcjFuture<Double>[] data = new PcjFuture[PCJ.threadCount()];
            for (int i = 1; i < PCJ.threadCount(); ++i) {
                data[i] = PCJ.asyncGet(i, SharedEnum.sum);
            }
            for (int i = 1; i < PCJ.threadCount(); ++i) {
                sum = sum + data[i].get();
            }

            return sum;
        } else {
            return Double.NaN;
        }
    }

    private void validate(double pi) {
        double refval = Math.PI;
        double dev = Math.abs(pi - refval);

        if (dev > 1.0e-5) {
            System.err.println("Validation failed");
            System.err.println("Value = " + pi + "  " + dev);
        }
    }
}
