/* 
 * Copyright (c) 2017, faramir
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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.pcj.PCJ;
import org.pcj.RegisterStorage;
import org.pcj.StartPoint;
import org.pcj.Storage;

@RegisterStorage(PiEstimator.Shared.class)
public class PiEstimator implements StartPoint {

    /**
     * Part of CloudEra Hadoop examples.
     *
     * com.cloudera.hadoop/hadoop-examples/0.20.2-737/
     * org/apache/hadoop/examples/PiEstimator.java
     *
     * 2-dimensional Halton sequence {H(i)}, where H(i) is a 2-dimensional point
     * and i >= 1 is the index. Halton sequence is used to generate sample
     * points for Pi estimation.
     */
    private static class HaltonSequence {

        /**
         * Bases
         */
        private static final int[] P = {2, 3};
        /**
         * Maximum number of digits allowed
         */
        private static final int[] K = {63, 40};

        private long index;
        private final double[] x;
        private final double[][] q;
        private final int[][] d;

        /**
         * Initialize to H(startindex), so the sequence begins with
         * H(startindex+1).
         */
        HaltonSequence(long startindex) {
            index = startindex;
            x = new double[K.length];
            q = new double[K.length][];
            d = new int[K.length][];
            for (int i = 0; i < K.length; i++) {
                q[i] = new double[K[i]];
                d[i] = new int[K[i]];
            }

            for (int i = 0; i < K.length; i++) {
                long k = index;
                x[i] = 0;

                for (int j = 0; j < K[i]; j++) {
                    q[i][j] = (j == 0 ? 1.0 : q[i][j - 1]) / P[i];
                    d[i][j] = (int) (k % P[i]);
                    k = (k - d[i][j]) / P[i];
                    x[i] += d[i][j] * q[i][j];
                }
            }
        }

        /**
         * Compute next point. Assume the current point is H(index). Compute
         * H(index+1).
         *
         * @return a 2-dimensional point with coordinates in [0,1)^2
         */
        double[] nextPoint() {
            index++;
            for (int i = 0; i < K.length; i++) {
                for (int j = 0; j < K[i]; j++) {
                    d[i][j]++;
                    x[i] += q[i][j];
                    if (d[i][j] < P[i]) {
                        break;
                    }
                    d[i][j] = 0;
                    x[i] -= (j == 0 ? 1.0 : q[i][j - 1]);
                }
            }
            return x;
        }
    }

    final private long[] numInsideArray = PCJ.myId() == 0 ? new long[PCJ.threadCount()] : null;
//    final private long[] numOutsideArray = PCJ.myId() == 0 ? new long[PCJ.threadCount()] : null;

    @Storage(PiEstimator.class)
    enum Shared {
        numInsideArray,
//        numOutsideArray,
    }

    private void calc(long offset, long size) {
        final HaltonSequence haltonsequence = new HaltonSequence(offset);
        long numInside = 0L;
//        long numOutside = 0L;

        for (long i = 0; i < size; ++i) {
            //generate points in a unit square
            final double[] point = haltonsequence.nextPoint();

            //count points inside/outside of the inscribed circle of the square
            final double x = point[0] - 0.5;
            final double y = point[1] - 0.5;
            if (x * x + y * y <= 0.25) {
                numInside++;
//            } else {
//                numOutside++;
            }
        }

        PCJ.asyncPut(numInside, 0, Shared.numInsideArray, PCJ.myId());
//        PCJ.put(numOutside, 0, Shared.numOutsideArray, PCJ.myId());
    }

    private final long[] time = new long[5];

    public BigDecimal estimate(int numMaps, long numPoints) throws IOException {
        long offset = PCJ.myId() * numPoints;
        calc(offset, numPoints);

        time[1] = System.nanoTime();

        if (PCJ.myId() == 0) {
            long numInside = 0L;
//            long numOutside = 0L;

            PCJ.waitFor(Shared.numInsideArray, PCJ.threadCount());
//            PCJ.waitFor(Shared.numOutsideArray, PCJ.threadCount());

            time[2] = System.nanoTime();

            for (long num : numInsideArray) {
                numInside += num;
            }
//            for (long num : numOutsideArray) {
//                numOutside += num;
//            }

            time[3] = System.nanoTime();

            return BigDecimal.valueOf(4).setScale(20)
                    .multiply(BigDecimal.valueOf(numInside))
                    .divide(BigDecimal.valueOf(numMaps), RoundingMode.HALF_EVEN)
                    .divide(BigDecimal.valueOf(numPoints), RoundingMode.HALF_EVEN);
        } else {
            return null;
        }
    }

    @Override
    public void main() throws Throwable {
        final int nMaps = PCJ.threadCount();
        
        estimate(nMaps, 1000); // warm-up
        
        final long nSamples = 100_000_000L;

        time[0] = System.nanoTime();
        BigDecimal pi = estimate(nMaps, nSamples);
        time[4] = System.nanoTime();

        if (PCJ.myId() == 0) {
            System.out.printf("PiEstimator\t%5d\ttime %12.7f%n",
                    PCJ.threadCount(),
                    (time[4] - time[0]) / 1e9
            );
        }

    }
}
