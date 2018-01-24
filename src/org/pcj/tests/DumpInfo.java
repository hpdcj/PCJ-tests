/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pcj.tests;

import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 *
 * @author faramir
 */
public class DumpInfo implements AutoCloseable {

    java.util.concurrent.ScheduledExecutorService pool;

    public DumpInfo() {
        int initDelay = 5;
        int delay = 0;
        try {
            delay = Integer.parseInt(System.getProperty("periodicallyDumpInfo", "0"));
        } catch (NumberFormatException e) {
        }
        
        if (delay > 0) {
            pool = Executors.newScheduledThreadPool(1);
            ThreadMXBean threadMXBean = java.lang.management.ManagementFactory.getThreadMXBean();
            MemoryMXBean memoryMXBean = java.lang.management.ManagementFactory.getMemoryMXBean();
            boolean objectMonitorUsage = threadMXBean.isObjectMonitorUsageSupported();
            boolean synchronizerUsage = threadMXBean.isSynchronizerUsageSupported();

            Function<Long, String> memoryToHuman = (size) -> {
                if (size < 0) {
                    return "undefined";
                }
                int exp = (int) (Math.log(size) / Math.log(1024));
                return String.format("%.1f %s", size / Math.pow(1024, exp), (exp > 1 ? "KMGTPE".charAt(exp - 1) : "") + "B");
            };

            pool.scheduleWithFixedDelay(() -> {
                System.err.println("\n **** DumpInfo on: " + new java.util.Date() + " **** \n");

                MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
                MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();
                System.err.printf(""
                        + "Heap memory usage:    init = %9s, used = %9s, commited = %9s, max = %s\n"
                        + "NonHeap memory usage: init = %9s, used = %9s, commited = %9s, max = %s\n"
                        + "Object pending finalization: %d\n\n",
                        memoryToHuman.apply(heapMemoryUsage.getInit()),
                        memoryToHuman.apply(heapMemoryUsage.getUsed()),
                        memoryToHuman.apply(heapMemoryUsage.getCommitted()),
                        memoryToHuman.apply(heapMemoryUsage.getMax()),
                        memoryToHuman.apply(nonHeapMemoryUsage.getInit()),
                        memoryToHuman.apply(nonHeapMemoryUsage.getUsed()),
                        memoryToHuman.apply(nonHeapMemoryUsage.getCommitted()),
                        memoryToHuman.apply(nonHeapMemoryUsage.getMax()),
                        memoryMXBean.getObjectPendingFinalizationCount()
                );

                java.util.Arrays
                        .stream(threadMXBean.dumpAllThreads(objectMonitorUsage, synchronizerUsage))
                        .forEach(System.err::print);
            }, initDelay, delay, java.util.concurrent.TimeUnit.SECONDS);
        }
    }

    @Override
    public void close() throws Exception {
        if (pool != null) {
            pool.shutdownNow();
        }
    }

}
