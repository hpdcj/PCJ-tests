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
package org.pcj.tests.app.mr;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PiEstimatorJava implements Runnable {

    private final static long NUM_POINTS = 1_000_000_000;
    private final static int THREAD_COUNT = 4;

    private static CountDownLatch latch;

    private final int id;
    private final long offset;
    private final long numPoints;
    private long numInside;

    public long getNumInside() {
        return numInside;
    }

    public PiEstimatorJava(int id, long offset, long numPoints) {
        this.id = id;
        this.offset = offset;
        this.numPoints = numPoints;
    }

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
    public static class HaltonSequence {

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
        public HaltonSequence(long startindex) {
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
        public double[] nextPoint() {
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

    private void calc(long offset, long size) {
        final HaltonSequence haltonsequence = new HaltonSequence(offset);
        numInside = 0L;

        for (long i = 0; i < size; ++i) {
            //generate points in a unit square

            final double[] point = haltonsequence.nextPoint();

            //count points inside/outside of the inscribed circle of the square
            final double x = point[0] - 0.5;
            final double y = point[1] - 0.5;
            if (x * x + y * y <= 0.25) {
                numInside++;
            }
        }
    }

    private final long[] time = new long[5];

    @Override
    public void run() {
        try {
            latch.countDown();
            latch.await();
            System.err.println("Starting thread " + id);
        } catch (InterruptedException ex) {
            Logger.getLogger(PiEstimatorJava.class.getName()).log(Level.SEVERE, null, ex);
        }

        time[0] = System.nanoTime();
        calc(offset, numPoints);
        time[1] = System.nanoTime();

        System.err.println("Thread " + id + ": " + (time[1] - time[0]) / 1e9 + "s");
    }

    private static long runExecution(long numPoints, int threadCount) {
        latch = new CountDownLatch(threadCount);

        ExecutorService threadPool = Executors.newFixedThreadPool(threadCount);

        PiEstimatorJava[] estimators = new PiEstimatorJava[threadCount];
        for (int i = 0; i < threadCount; ++i) {
            estimators[i] = new PiEstimatorJava(i, i * numPoints, numPoints);
        }

        long time = System.nanoTime();
        Arrays.stream(estimators).forEach(threadPool::execute);
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            Logger.getLogger(PiEstimatorJava.class.getName()).log(Level.SEVERE, null, ex);
        }

        long numInside = Arrays.stream(estimators).mapToLong(PiEstimatorJava::getNumInside).sum();
        time = System.nanoTime() - time;

        BigDecimal pi = BigDecimal.valueOf(4).setScale(20)
                .multiply(BigDecimal.valueOf(numInside))
                .divide(BigDecimal.valueOf(threadCount), RoundingMode.HALF_EVEN)
                .divide(BigDecimal.valueOf(numPoints), RoundingMode.HALF_EVEN);
        System.out.println("Pi = " + pi + " using " + numPoints + " points per thread and " + threadCount + " threads ");

        return time;
    }
// run.jvmargs=-XX:+UnlockDiagnosticVMOptions -XX:+PrintInlining -XX:+PrintGC -XX:+PrintCompilation

    public static void main(String[] args) {
//        runExecution(1000_000_000, 1);
//        System.err.println("---");

        List<Long> times = new ArrayList<>();
        int nTimes = 3;
        for (int i = 1; i <= nTimes; ++i) {
            System.err.println("Starting execution: " + i + " of " + nTimes);
            long time = runExecution(NUM_POINTS, THREAD_COUNT);
            times.add(time);
            System.err.println("Time taken: " + time / 1e9 + "s");
            System.err.println("-------------------------");

        }
        System.out.println("Min time: " + (times.stream().mapToLong(Long::longValue).min().orElse(0)) / 1e9 + "s");
        System.out.println("Avg time: " + (times.stream().mapToLong(Long::longValue).sum() / nTimes) / 1e9 + "s");
        System.out.println("Max time: " + (times.stream().mapToLong(Long::longValue).max().orElse(0)) / 1e9 + "s");
        System.out.println("Median: " + (times.stream().mapToLong(Long::longValue).sorted().skip((nTimes - 1) / 2).limit(nTimes % 2 == 0 ? 2 : 1).average().orElse(0)) / 1e9 + "s");
    }
}
// run.jvmargs=-Xcomp -Xbatch -XX:+PrintCompilation -XX:+UnlockDiagnosticVMOptions -XX\+PrintInlining