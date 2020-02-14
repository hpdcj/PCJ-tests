/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pcj.tests;

import java.lang.management.LockInfo;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * @author faramir
 */
public class DumpInfo implements AutoCloseable {

    private java.util.concurrent.ScheduledExecutorService pool;

    public DumpInfo() {
        int initDelay = 0;
        int delay = 0;
        try {
            delay = Integer.parseInt(System.getProperty("dumpInfo.delay", "0"));
            delay = Integer.parseInt(System.getProperty("dumpInfo.init", "0"));
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
                        .sorted(Comparator.comparing(ThreadInfo::getThreadName))
                        .filter(threadInfo -> threadInfo.getThreadName().matches("^PcjThread-[0-9]*$"))
                        .map(DumpInfo::printThreadInfo)
                        .forEach(System.err::println);

            }, initDelay, delay, java.util.concurrent.TimeUnit.SECONDS);
        }
    }

    @Override
    public void close() {
        if (pool != null) {
            pool.shutdownNow();
        }
    }

    private static String printThreadInfo(ThreadInfo threadInfo) {
        StringBuilder sb = new StringBuilder("\"" + threadInfo.getThreadName() + "\"" +
//                                                     (threadInfo.isDaemon() ? " daemon" : "") +
//                                                     " prio=" + threadInfo.getPriority() +
                                                     " Id=" + threadInfo.getThreadId() + " " +
                                                     threadInfo.getThreadState());
        if (threadInfo.getLockName() != null) {
            sb.append(" on " + threadInfo.getLockName());
        }
        if (threadInfo.getLockOwnerName() != null) {
            sb.append(" owned by \"" + threadInfo.getLockOwnerName() +
                              "\" Id=" + threadInfo.getLockOwnerId());
        }
        if (threadInfo.isSuspended()) {
            sb.append(" (suspended)");
        }
        if (threadInfo.isInNative()) {
            sb.append(" (in native)");
        }
        sb.append('\n');
        int i = 0;
        StackTraceElement[] stackTrace = threadInfo.getStackTrace();
        for (; i < stackTrace.length; i++) {
            StackTraceElement ste = stackTrace[i];
            sb.append("\tat " + ste.toString());
            sb.append('\n');
            if (i == 0 && threadInfo.getLockInfo() != null) {
                Thread.State ts = threadInfo.getThreadState();
                switch (ts) {
                    case BLOCKED:
                        sb.append("\t-  blocked on " + threadInfo.getLockInfo());
                        sb.append('\n');
                        break;
                    case WAITING:
                        sb.append("\t-  waiting on " + threadInfo.getLockInfo());
                        sb.append('\n');
                        break;
                    case TIMED_WAITING:
                        sb.append("\t-  waiting on " + threadInfo.getLockInfo());
                        sb.append('\n');
                        break;
                    default:
                }
            }

            for (MonitorInfo mi : threadInfo.getLockedMonitors()) {
                if (mi.getLockedStackDepth() == i) {
                    sb.append("\t-  locked " + mi);
                    sb.append('\n');
                }
            }
        }

        LockInfo[] locks = threadInfo.getLockedSynchronizers();
        if (locks.length > 0) {
            sb.append("\n\tNumber of locked synchronizers = " + locks.length);
            sb.append('\n');
            for (LockInfo li : locks) {
                sb.append("\t- " + li);
                sb.append('\n');
            }
        }
        sb.append('\n');
        return sb.toString();
    }
}
