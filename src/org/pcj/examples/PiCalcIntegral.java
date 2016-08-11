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
package org.pcj.examples;

import org.pcj.NodesDescription;
import org.pcj.PCJ;
import org.pcj.PcjFuture;
import org.pcj.Shared;
import org.pcj.StartPoint;
import static org.pcj.examples.PiCalcIntegral.SharedEnum.*;

/**
 *
 * @author faramir
 */
public class PiCalcIntegral implements StartPoint {

    public enum SharedEnum implements Shared {
        sum;
    }

    private double f(double x) {
        return (4.0 / (1.0 + x * x));
    }

    @Override
    public void main() throws Throwable {
        int points = 100_000_000;

        double pi = calc(points);

        if (PCJ.myId() == 0) {

            System.out.format("%f%n", pi);
        }
    }

    private double calc(int N) {
        double w;

        w = 1.0 / (double) N;
        double localSum = 0.0;
        for (int i = PCJ.myId() + 1; i <= N; i += PCJ.threadCount()) {
            localSum = localSum + f(((double) i - 0.5) * w);
        }
        localSum = localSum * w;
        PCJ.putLocal(sum, localSum);

        PCJ.barrier();
        if (PCJ.myId() == 0) {
            PcjFuture<Double>[] data = new PcjFuture[PCJ.threadCount()];
            for (int i = 1; i < PCJ.threadCount(); ++i) {
                data[i] = PCJ.asyncGet(i, sum);
            }
            for (int i = 1; i < PCJ.threadCount(); ++i) {
                localSum = localSum + data[i].get();
            }

            return localSum;
        } else {
            return Double.NaN;
        }
    }

    public static void main(String[] args) {
        PCJ.deploy(PiCalcIntegral.class,
                new NodesDescription(
                        new String[]{"localhost", "localhost", "localhost"}),
                SharedEnum.class);
    }
}
