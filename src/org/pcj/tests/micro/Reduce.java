/*
 * Copyright (c) 2019, Marek Nowicki
 * All rights reserved.
 *
 * Licensed under New BSD License (3-clause license).
 *
 * See the file "LICENSE" for the full license governing this code.
 */
package org.pcj.tests.micro;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.pcj.PCJ;
import org.pcj.PcjFuture;
import org.pcj.RegisterStorage;
import org.pcj.StartPoint;
import org.pcj.Storage;

/**
 * @author Marek Nowicki (faramir@mat.umk.pl)
 */
@RegisterStorage(Reduce.Vars.class)
public class Reduce implements StartPoint {

    private static final int NUMBER_OF_TESTS = 20;
    private static final int REPEAT_TEST_TIMES = 100;

    @Storage(Reduce.class)
    enum Vars {
        value,
        valueArray,
        treeArray
    }

    private double value;
    private double[] valueArray = PCJ.myId() == 0 ? new double[PCJ.threadCount()] : null;
    private double[] treeArray = new double[2];
    private List<Random> expectedRandomList = null;

    private double calculateExpectedValue() {
        if (PCJ.myId() == 0) {
            return expectedRandomList.stream()
                           .mapToDouble(Random::nextDouble)
                           .sum();
        }
        return 0;
    }

    private List<Random> prepareRandomList() {
        if (PCJ.myId() == 0) {
            return IntStream.range(0, PCJ.threadCount())
                           .mapToObj(Random::new)
                           .collect(Collectors.toList());
        }
        return null;
    }

    private static class Benchmark {
        private final String name;
        private final Supplier<Double> method;
        private final List<Long> times;

        private Benchmark(String name, Supplier<Double> method) {
            this.name = name;
            this.method = method;

            times = new ArrayList<>();
        }

        public long test(double expectedValue) {

            double value = Double.NaN;

            long start = System.nanoTime();
            for (int i = 0; i < REPEAT_TEST_TIMES; ++i) {
                value = method.get();
            }
            long elapsed = System.nanoTime() - start;
            times.add(elapsed / REPEAT_TEST_TIMES);

            if (PCJ.myId() == 0 && !equalsDouble(expectedValue, value)) {
                System.err.println("Verification failed: " + value + " != " + expectedValue + " for " + name);
            }
            return elapsed;
        }

        private static boolean equalsDouble(double d1, double d2) {
            return Math.abs(d1 - d2) / Math.max(Math.abs(d1), Math.abs(d2)) < 1e-8;
        }

        @Override
        public String toString() {
            return String.format("%-11s = %.7f",
                    name,
                    times.stream().mapToLong(Long::longValue).min().orElse(0) / 1e9
            );
        }
    }

    @Override
    public void main() {
        Locale.setDefault(Locale.ENGLISH);
        expectedRandomList = prepareRandomList();

        Random r = new Random(PCJ.myId());

        Benchmark[] benchmarks = new Benchmark[]{
                new Benchmark("pcjGet", this::pcjGet),
                new Benchmark("pcjAsyncGet", this::pcjAsyncGet),
                new Benchmark("pcjPut", this::pcjPut),
                new Benchmark("pcjTreePut", this::pcjTreePut),
                new Benchmark("pcjGather", this::pcjGather),
                new Benchmark("pcjCollect", this::pcjCollect),
                new Benchmark("pcjReduce", this::pcjReduce),
        };

        for (int i = 0; i < NUMBER_OF_TESTS; ++i) {
            if (PCJ.myId() == 0) {
                System.err.println("------ " + (i + 1) + " of " + NUMBER_OF_TESTS + " ------");
            }
            double expectedValue = calculateExpectedValue();
            value = r.nextDouble();
            PCJ.barrier();

            for (Benchmark benchmark : benchmarks) {
                for (Vars var : Vars.values()) {
                    PCJ.monitor(var);
                }
                PCJ.barrier();

                long elapsed = benchmark.test(expectedValue);
                if (PCJ.myId() == 0) {
                    System.err.println(benchmark.name + "\t" + elapsed);
                }
            }

            PCJ.barrier();
        }
        if (PCJ.myId() == 0) {
            for (Benchmark benchmark : benchmarks) {
                System.out.println(PCJ.getNodeCount() + "\t" + PCJ.threadCount() + "\t" + benchmark);
            }
        }
    }


    private double pcjGet() {
        if (PCJ.myId() == 0) {
            double sum = 0.0;
            for (int i = 0; i < PCJ.threadCount(); i++) {
                sum += PCJ.<Double>get(i, Vars.value);
            }
            return sum;
        }
        return 0;
    }

    private double pcjAsyncGet() {
        if (PCJ.myId() == 0) {
            Set<PcjFuture<Double>> futures = new HashSet<>();
            for (int i = 0; i < PCJ.threadCount(); i++) {
                futures.add(PCJ.asyncGet(i, Vars.value));
            }
            return futures.stream().mapToDouble(PcjFuture::get).sum();
        }
        return 0;
    }

    private double pcjPut() {
        PCJ.put(value, 0, Vars.valueArray, PCJ.myId());
        if (PCJ.myId() == 0) {
            PCJ.waitFor(Vars.valueArray, PCJ.threadCount());

            return Arrays.stream(valueArray).sum();
        }
        return 0;
    }

    private double pcjTreePut() {
        int childCount = Math.min(2, Math.max(0, PCJ.threadCount() - PCJ.myId() * 2 - 1));
        PCJ.waitFor(Vars.treeArray, childCount);
        if (PCJ.myId() > 0) {
            double v = value + Arrays.stream(treeArray).limit(childCount).sum();
            PCJ.put(v, (PCJ.myId() - 1) / 2, Vars.treeArray, (PCJ.myId() + 1) % 2);
        }
        if (PCJ.myId() == 0) {
            return value + Arrays.stream(treeArray).limit(childCount).sum();
        }
        return 0;
    }

    private double pcjGather() {
        if (PCJ.myId() == 0) {
            double[] values = PCJ.gather(Vars.value).values().stream().mapToDouble(d -> (double)d).toArray();
            return Arrays.stream(values).sum();
        }
        return 0;
    }

    private double pcjCollect() {
        if (PCJ.myId() == 0) {
            List<Double> values = PCJ.collect(Collectors::toList, Vars.value);
            return values.stream().mapToDouble(Double::doubleValue).sum();
        }
        return 0;
    }

    private double pcjReduce() {
        if (PCJ.myId() == 0) {
            return PCJ.reduce(Double::sum, Vars.value);
        }
        return 0;
    }
}
